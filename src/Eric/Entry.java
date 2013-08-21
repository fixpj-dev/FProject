package Eric;

import java.io.IOException;
import java.util.Scanner;

import quickfix.*;
import quickfix.field.ClOrdID;
import quickfix.field.HandlInst;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix50.NewOrderSingle;

public class Entry {
	public static void main(String[] args) {
        SocketInitiator socketInitiator = null;
        int tryCount = 0;
        try {
            SessionSettings initiatorSettings = new SessionSettings("D:/imsi/GitHub/FProject/src/Eric/ClientSettings.txt");
            Application initiatorApplication = new TradeClient();
            FileStoreFactory fileStoreFactory = new FileStoreFactory(initiatorSettings);
            FileLogFactory fileLogFactory = new FileLogFactory(initiatorSettings);
            MessageFactory messageFactory = new DefaultMessageFactory();
            socketInitiator = new SocketInitiator(initiatorApplication, fileStoreFactory, initiatorSettings, fileLogFactory, messageFactory);
            socketInitiator.start();
            SessionID sessionId = socketInitiator.getSessions().get(0);
            Session.lookupSession(sessionId).logon();
            while(!Session.lookupSession(sessionId).isLoggedOn()){
            	tryCount++;
                System.out.println("Waiting for login success");
                Thread.sleep(3000);
                if(tryCount == 3){
                	socketInitiator.stop();
                	System.exit(0);
                	break;
                }
            }
            System.out.println("Logged In...");
            Thread.sleep(5000);
            bookSingleOrder(sessionId);
             
            System.out.println("Type to quit");
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
            scanner.next();
            Session.lookupSession(sessionId).disconnect("Done",false);
            socketInitiator.stop();
        } catch (ConfigError e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	private static void bookSingleOrder(SessionID sessionID){
        //In real world this won't be a hardcoded value rather than a sequence.
        ClOrdID orderId = new ClOrdID("1");
        //to be executed on the exchange
        @SuppressWarnings("unused")
		HandlInst instruction = new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE);
        //Since its FX currency pair name
        @SuppressWarnings("unused")
		Symbol mainCurrency = new Symbol("EUR/USD");
        //Which side buy, sell
        Side side = new Side(Side.BUY);
        //Time of transaction
        TransactTime transactionTime = new TransactTime();
        //Type of our order, here we are assuming this is being executed on the exchange
        OrdType orderType = new OrdType(OrdType.FOREX_MARKET);
        NewOrderSingle newOrderSingle = new NewOrderSingle(orderId,side, transactionTime,orderType);
        //Quantity
        newOrderSingle.set(new OrderQty(100));
        try {
            Session.sendToTarget(newOrderSingle, sessionID);
        } catch (SessionNotFound e) {
            e.printStackTrace();
        }
    }
}