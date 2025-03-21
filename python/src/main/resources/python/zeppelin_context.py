#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import os, sys
import warnings
import base64

from io import BytesIO

try:
    from StringIO import StringIO
except ImportError:
    from io import StringIO

class PyZeppelinContext(object):
    """ A context impl that uses Py4j to communicate to JVM
    """

    def __init__(self, z, gateway):
        self.z = z
        self.gateway = gateway
        self.paramOption = gateway.jvm.com.teragrep.zep_01.display.ui.OptionInput.ParamOption
        self.javaList = gateway.jvm.java.util.ArrayList
        self.max_result = z.getMaxResult()
        self._displayhook = lambda *args: None
        self._setup_matplotlib()

    # By implementing special methods it makes operating on it more Pythonic
    def __setitem__(self, key, item):
        self.z.put(key, item)

    def __getitem__(self, key):
        return self.z.get(key)

    def __delitem__(self, key):
        self.z.remove(key)

    def __contains__(self, item):
        return self.z.containsKey(item)

    def put(self, key, value):
        self.__setitem__(key, value)

    def get(self, key):
        return self.__getitem__(key)

    def getAsDataFrame(self, key):
        value = self.get(key)
        try:
            import pandas as pd
        except ImportError:
            print("fail to call getAsDataFrame as pandas is not installed")
        return pd.read_csv(StringIO(value), sep="\t")

    def remove(self, key):
        self.z.remove(key)

    def angular(self, key, noteId = None, paragraphId = None):
        return self.z.angular(key, noteId, paragraphId)

    def contains(self, key):
        return self.contains(key)

    def add(self, key, value):
        self.__setitem__(key, value)

    def getInterpreterContext(self):
        return self.z.getInterpreterContext()

    def input(self, name, defaultValue=""):
        return self.z.input(name, defaultValue)

    def textbox(self, name, defaultValue=""):
        return self.z.textbox(name, defaultValue)

    def noteTextbox(self, name, defaultValue=""):
        return self.z.noteTextbox(name, defaultValue)

    def password(self, name):
        return self.z.password(name)

    def notePassword(self, name):
        return self.z.notePassword(name)

    def select(self, name, options, defaultValue=""):
        return self.z.select(name, self.getParamOptions(options), defaultValue)

    def noteSelect(self, name, options, defaultValue=""):
        return self.z.noteSelect(name, self.getParamOptions(options), defaultValue)

    def checkbox(self, name, options, defaultChecked=[]):
        return self.z.checkbox(name, self.getParamOptions(options), self.getDefaultChecked(defaultChecked))

    def noteCheckbox(self, name, options, defaultChecked=[]):
        return self.z.noteCheckbox(name, self.getParamOptions(options), self.getDefaultChecked(defaultChecked))

    def run(self, paragraphId):
        return self.z.run(paragraphId)

    def run(self, noteId, paragraphId):
        return self .z.run(noteId, paragraphId)

    def runNote(self, noteId):
        return self.z.runNote(noteId)

    def runAll(self):
        return self.z.runAll()

    def angular(self, name, noteId = None, paragraphId = None):
        if noteId == None:
            return self.z.angular(name, self.z.getInterpreterContext().getNoteId(), paragraphId)
        else:
            return self.z.angular(name, noteId, paragraphId)

    def angularBind(self, name, value, noteId = None, paragraphId = None):
        if noteId == None:
            return self.z.angularBind(name, value, noteId, paragraphId)
        else:
            return self.z.angularBind(name, value, self.z.getInterpreterContext().getNoteId(), paragraphId)

    def angularUnbind(self, name, noteId = None):
        if noteId == None:
            self.z.angularUnbind(name, self.z.getInterpreterContext().getNoteId())
        else:
            self.z.angularUnbind(name, noteId)

    def registerHook(self, event, cmd, replName=None):
        if replName is None:
            self.z.registerHook(event, cmd)
        else:
            self.z.registerHook(event, cmd, replName)

    def unregisterHook(self, event, replName=None):
        if replName is None:
            self.z.unregisterHook(event)
        else:
            self.z.unregisterHook(event, replName)

    def registerNoteHook(self, event, cmd, noteId, replName=None):
        if replName is None:
            self.z.registerNoteHook(event, cmd, noteId)
        else:
            self.z.registerNoteHook(event, cmd, noteId, replName)

    def unregisterNoteHook(self, event, noteId, replName=None):
        if replName is None:
            self.z.unregisterNoteHook(event, noteId)
        else:
            self.z.unregisterNoteHook(event, noteId, replName)

    def getParamOptions(self, options):
        javaOptions = self.gateway.new_array(self.paramOption, len(options))
        i = 0
        for tuple in options:
            javaOptions[i] = self.paramOption(tuple[0], tuple[1])
            i += 1
        return javaOptions

    def getDefaultChecked(self, defaultChecked):
        javaDefaultChecked = self.javaList()
        for check in defaultChecked:
            javaDefaultChecked.append(check)
        return javaDefaultChecked

    def show(self, p, **kwargs):
        if hasattr(p, '__name__') and p.__name__ == "matplotlib.pyplot":
            self.show_matplotlib(p, **kwargs)
        elif any(t.__name__ == 'DataFrame' for t in type(p).mro()):
            # `isinstance(p, DataFrame)` would req `import pandas.core.frame.DataFrame`
            # and so a dependency on pandas
            self.show_dataframe(p, **kwargs)
        else:
            print(str(p))

    def normalizeColumn(self, column):
        return column.replace("\t", " ").replace("\r\n", " ").replace("\n", " ")

    def show_dataframe(self, df, **kwargs):
        """Pretty prints DF using Table Display System
        """
        show_index = False
        if 'show_index' in kwargs:
            show_index = kwargs['show_index']

        exceed_limit = len(df) > self.max_result
        header_buf = StringIO("")
        if show_index:
            idx_name = str(df.index.name) if df.index.name is not None else ""
            header_buf.write(self.normalizeColumn(idx_name) + "\t")
        header_buf.write(self.normalizeColumn(str(df.columns[0])))
        for col in df.columns[1:]:
            header_buf.write("\t")
            header_buf.write(self.normalizeColumn(str(col)))
        header_buf.write("\n")

        body_buf = StringIO("")
        rows = df.head(self.max_result).values if exceed_limit else df.values
        rowNumber = len(rows)
        index = df.index.values
        for idx, row in zip(index, rows):
            if show_index:
                body_buf.write("%html <strong>{}</strong>".format(idx))
                body_buf.write("\t")
            body_buf.write(self.normalizeColumn(str(row[0])))
            for cell in row[1:]:
                body_buf.write("\t")
                body_buf.write(self.normalizeColumn(str(cell)))
            # don't print '\n' after the last row
            rowNumber -=1
            if rowNumber != 0:
                body_buf.write("\n")
        body_buf.seek(0)
        header_buf.seek(0)
        print("%table " + header_buf.read() + body_buf.read())
        body_buf.close()
        header_buf.close()
        if exceed_limit:
            print("\n%html <font color=red>Results are limited by {}.</font>".format(self.max_result))

    def show_matplotlib(self, p, fmt="png", width="auto", height="auto",
                        **kwargs):
        """Matplotlib show function
        """
        if fmt == "png":
            img = BytesIO()
            p.savefig(img, format=fmt)
            img_str = b"data:image/png;base64,"
            img_str += base64.b64encode(img.getvalue().strip())
            img_tag = "<img src={img} style='width={width};height:{height}'>"
            # Decoding is necessary for Python 3 compatibility
            img_str = img_str.decode("ascii")
            img_str = img_tag.format(img=img_str, width=width, height=height)
        elif fmt == "svg":
            img = StringIO()
            p.savefig(img, format=fmt)
            img_str = img.getvalue()
        else:
            raise ValueError("fmt must be 'png' or 'svg'")

        html = "%html <div style='width:{width};height:{height}'>{img}<div>"
        print(html.format(width=width, height=height, img=img_str))
        img.close()

    def configure_mpl(self, **kwargs):
        import mpl_config
        mpl_config.configure(**kwargs)

    def _setup_matplotlib(self):
        # If we don't have matplotlib installed don't bother continuing
        try:
            import matplotlib
        except ImportError:
            return

        # Make sure custom backends are available in the PYTHONPATH
        rootdir = os.environ.get('ZEPPELIN_HOME', os.getcwd())
        mpl_path = os.path.join(rootdir, 'interpreter', 'lib', 'python')
        if mpl_path not in sys.path:
            sys.path.append(mpl_path)

        # Finally check if backend exists, and if so configure as appropriate
        try:
            matplotlib.use('module://backend_zinline')
            import backend_zinline

            # Everything looks good so make config assuming that we are using
            # an inline backend
            self._displayhook = backend_zinline.displayhook
            self.configure_mpl(width=600, height=400, dpi=72, fontsize=10,
                               interactive=True, format='png', context=self.z)
        except ImportError:
            # Fall back to Agg if no custom backend installed
            matplotlib.use('Agg')
            warnings.warn("Unable to load inline matplotlib backend, "
                          "falling back to Agg")
