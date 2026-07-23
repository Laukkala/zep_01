package com.teragrep.zep_01.interpreter.remote;

import com.teragrep.zep_01.display.AngularObject;
import com.teragrep.zep_01.display.AngularObjectRegistryListener;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterResultMessage;
import com.teragrep.zep_01.interpreter.thrift.LibraryMetadata;
import com.teragrep.zep_01.interpreter.thrift.ParagraphInfo;
import com.teragrep.zep_01.interpreter.thrift.RegisterInfo;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterEventService;
import com.teragrep.zep_01.resource.Resource;
import com.teragrep.zep_01.resource.ResourceId;
import com.teragrep.zep_01.resource.ResourcePoolConnector;
import com.teragrep.zep_01.resource.ResourceSet;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface InterpreterEventClient extends ResourcePoolConnector, AngularObjectRegistryListener, AutoCloseable {
    <R> R callRemoteFunction(PooledRemoteClient.RemoteFunction<R, RemoteInterpreterEventService.Client> func);

    void setIntpGroupId(String intpGroupId);

    void registerInterpreterProcess(RegisterInfo registerInfo);

    void unRegisterInterpreterProcess();

    void sendWebUrlInfo(String webUrl);

    @Override
    ResourceSet getAllResources();

    List<ParagraphInfo> getParagraphList(String user, String noteId);

    List<LibraryMetadata> getAllLibraryMetadatas(String interpreter);

    ByteBuffer getLibrary(String interpreter, String libraryName);

    @Override
    Object readResource(ResourceId resourceId);

    @Override
    Object invokeMethod(
            ResourceId resourceId,
            String methodName,
            Class[] paramTypes,
            Object[] params);

    @Override
    Resource invokeMethod(
            ResourceId resourceId,
            String methodName,
            Class[] paramTypes,
            Object[] params,
            String returnResourceName);

    void onInterpreterOutputAppend(
            String noteId, String paragraphId, int outputIndex, String output);

    void onInterpreterOutputUpdate(
            String noteId, String paragraphId, int outputIndex,
            InterpreterResult.Type type, String output);

    void onInterpreterOutputUpdateAll(
            String noteId, String paragraphId, List<InterpreterResultMessage> messages);

    void runParagraphs(String noteId,
                       List<String> paragraphIds,
                       List<Integer> paragraphIndices,
                       String curParagraphId);

    void checkpointOutput(String noteId, String paragraphId);

    void onParaInfosReceived(Map<String, String> infos);

    @Override
    void onAddAngularObject(String interpreterGroupId, AngularObject angularObject);

    @Override
    void onUpdateAngularObject(String interpreterGroupId, AngularObject angularObject);

    @Override
    void onRemoveAngularObject(String interpreterGroupId, AngularObject angularObject);

    void updateParagraphConfig(String noteId, String paragraphId, Map<String, String> config);

    @Override
    void close();
}
