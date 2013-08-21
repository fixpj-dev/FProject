package Server;

import Server.TradeExecutor;
import quickfix.*;

public class Entry {
	public static void main(String[] args) {
        SocketAcceptor socketAcceptor = null;
        try {
            SessionSettings executorSettings = new SessionSettings(new java.io.File("src/Server/ServerSettings.txt").getAbsolutePath());
            Application application = new TradeExecutor();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(executorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            FileLogFactory fileLogFactory = new FileLogFactory(executorSettings);
            socketAcceptor = new SocketAcceptor(application, fileStoreFactory, executorSettings, fileLogFactory, messageFactory);
            socketAcceptor.start();
            try{
            	Thread.sleep(20000);
            	socketAcceptor.stop();
            }catch(Exception e){
            	System.out.println(e.getMessage());
            }
            
        } catch (ConfigError e) {
            e.printStackTrace();
        }
    }
}
