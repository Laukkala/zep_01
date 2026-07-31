package com.teragrep.zep_01.fakes;

import com.teragrep.zep_01.display.AngularObject;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterResultMessage;
import com.teragrep.zep_01.interpreter.remote.PooledRemoteClient;
import com.teragrep.zep_01.interpreter.remote.InterpreterEventClient;
import com.teragrep.zep_01.interpreter.thrift.LibraryMetadata;
import com.teragrep.zep_01.interpreter.thrift.ParagraphInfo;
import com.teragrep.zep_01.interpreter.thrift.RegisterInfo;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterEventService;
import com.teragrep.zep_01.resource.Resource;
import com.teragrep.zep_01.resource.ResourceId;
import com.teragrep.zep_01.resource.ResourceSet;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class FakeRemoteInterpreterEventClient implements InterpreterEventClient {
    private final AtomicReference<Boolean> unregistered;
    public FakeRemoteInterpreterEventClient(){
        unregistered = new AtomicReference<>(false);
    }

    @Override
    public <R> R callRemoteFunction(final PooledRemoteClient.RemoteFunction<R, RemoteInterpreterEventService.Client> func) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void setIntpGroupId(final String intpGroupId) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void registerInterpreterProcess(final RegisterInfo registerInfo) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void unRegisterInterpreterProcess() {
        unregistered.set(true);
    }

    public boolean unregistered(){
        return unregistered.get();
    }

    @Override
    public void sendWebUrlInfo(final String webUrl) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public ResourceSet getAllResources() {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public List<ParagraphInfo> getParagraphList(final String user, final String noteId) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public List<LibraryMetadata> getAllLibraryMetadatas(final String interpreter) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public ByteBuffer getLibrary(final String interpreter, final String libraryName) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public Object readResource(final ResourceId resourceId) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public Object invokeMethod(final ResourceId resourceId, final String methodName, final Class[] paramTypes, final Object[] params) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public Resource invokeMethod(final ResourceId resourceId, final String methodName, final Class[] paramTypes, final Object[] params, final String returnResourceName) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onInterpreterOutputAppend(final String noteId, final String paragraphId, final int outputIndex, final String output) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onInterpreterOutputUpdate(final String noteId, final String paragraphId, final int outputIndex, final InterpreterResult.Type type, final String output) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onInterpreterOutputUpdateAll(final String noteId, final String paragraphId, final List<InterpreterResultMessage> messages) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void runParagraphs(final String noteId, final List<String> paragraphIds, final List<Integer> paragraphIndices, final String curParagraphId) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void checkpointOutput(final String noteId, final String paragraphId) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onParaInfosReceived(final Map<String, String> infos) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onAddAngularObject(final String interpreterGroupId, final AngularObject angularObject) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onUpdateAngularObject(final String interpreterGroupId, final AngularObject angularObject) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void onRemoveAngularObject(final String interpreterGroupId, final AngularObject angularObject) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void updateParagraphConfig(final String noteId, final String paragraphId, final Map<String, String> config) {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public void close() {
        throw new RuntimeException("Not implemented by fake");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FakeRemoteInterpreterEventClient that = (FakeRemoteInterpreterEventClient) o;
        return unregistered == that.unregistered;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(unregistered);
    }
}