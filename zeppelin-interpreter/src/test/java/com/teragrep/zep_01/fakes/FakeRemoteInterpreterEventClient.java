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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FakeRemoteInterpreterEventClient implements InterpreterEventClient {
    private boolean unregistered = false;
    public FakeRemoteInterpreterEventClient(){
    }

    @Override
    public <R> R callRemoteFunction(PooledRemoteClient.RemoteFunction<R, RemoteInterpreterEventService.Client> func) {
        return null; 
    }

    @Override
    public void setIntpGroupId(String intpGroupId) {
        
    }

    @Override
    public void registerInterpreterProcess(RegisterInfo registerInfo) {
        
    }

    @Override
    public void unRegisterInterpreterProcess() {
        unregistered = true;
    }

    @Override
    public void sendWebUrlInfo(String webUrl) {
        
    }

    @Override
    public ResourceSet getAllResources() {
        return new ResourceSet();
    }

    @Override
    public List<ParagraphInfo> getParagraphList(String user, String noteId) {
        return new ArrayList<>();
    }

    @Override
    public List<LibraryMetadata> getAllLibraryMetadatas(String interpreter) {
        return new ArrayList<>();
    }

    @Override
    public ByteBuffer getLibrary(String interpreter, String libraryName) {
        return ByteBuffer.allocate(0);
    }

    @Override
    public Object readResource(ResourceId resourceId) {
        return new Object();
    }

    @Override
    public Object invokeMethod(ResourceId resourceId, String methodName, Class[] paramTypes, Object[] params) {
        return new Object();
    }

    @Override
    public Resource invokeMethod(ResourceId resourceId, String methodName, Class[] paramTypes, Object[] params, String returnResourceName) {
        return null; 
    }

    @Override
    public void onInterpreterOutputAppend(String noteId, String paragraphId, int outputIndex, String output) {
        
    }

    @Override
    public void onInterpreterOutputUpdate(String noteId, String paragraphId, int outputIndex, InterpreterResult.Type type, String output) {
        
    }

    @Override
    public void onInterpreterOutputUpdateAll(String noteId, String paragraphId, List<InterpreterResultMessage> messages) {
        
    }

    @Override
    public void runParagraphs(String noteId, List<String> paragraphIds, List<Integer> paragraphIndices, String curParagraphId) {
        
    }

    @Override
    public void checkpointOutput(String noteId, String paragraphId) {
        
    }

    @Override
    public void onParaInfosReceived(Map<String, String> infos) {
        
    }

    @Override
    public void onAddAngularObject(String interpreterGroupId, AngularObject angularObject) {
        
    }

    @Override
    public void onUpdateAngularObject(String interpreterGroupId, AngularObject angularObject) {
        
    }

    @Override
    public void onRemoveAngularObject(String interpreterGroupId, AngularObject angularObject) {
        
    }

    @Override
    public void updateParagraphConfig(String noteId, String paragraphId, Map<String, String> config) {
        
    }

    @Override
    public void close() {
        
    }

    public boolean unregistered(){
        return unregistered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeRemoteInterpreterEventClient that = (FakeRemoteInterpreterEventClient) o;
        return unregistered == that.unregistered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unregistered);
    }
}