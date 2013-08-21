package Server;

import java.util.HashMap;
import java.util.Map;

import quickfix.*;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.fix50.ExecutionReport;
import quickfix.fix50.NewOrderSingle;

public class TradeExecutor extends MessageCracker implements Application {
    
	private Map<String, Double> priceMap = null;
	private int ordID, execID;
	
	private String GenOrdID() {
		return Integer.toString(++ordID);
	}
	private String GenExecID() {
		return Integer.toString(++execID);
	}
	public TradeExecutor() {
		priceMap = new HashMap<String, Double>();
		priceMap.put("EUR/USD", 1.243);
	}
   /** (non-Javadoc)
    * @see quickfix.Application#onCreate(quickfix.SessionID)
    */
   @Override
   public void onCreate(SessionID sessionId) {
       System.out.println("Executor Session Created with SessionID = " + sessionId);
   }

   /** (non-Javadoc)
    * @see quickfix.Application#onLogon(quickfix.SessionID)
    */
   @Override
   public void onLogon(SessionID sessionId) {

   }

   /** (non-Javadoc)
    * @see quickfix.Application#onLogout(quickfix.SessionID)
    */
   @Override
   public void onLogout(SessionID sessionId) {

   }

   /** (non-Javadoc)
    * @see quickfix.Application#toAdmin(quickfix.Message, quickfix.SessionID)
    */
   @Override
   public void toAdmin(Message message, SessionID sessionId) {

   }

   /** (non-Javadoc)
    * @see quickfix.Application#fromAdmin(quickfix.Message, quickfix.SessionID)
    */
   @Override
   public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

   }

   /** (non-Javadoc)
    * @see quickfix.Application#toApp(quickfix.Message, quickfix.SessionID)
    */
   @Override
   public void toApp(Message message, SessionID sessionId) throws DoNotSend {

   }

   /** (non-Javadoc)
    * @see quickfix.Application#fromApp(quickfix.Message, quickfix.SessionID)
    */
   @Override
   public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
	   crack(message, sessionId);
   }
   
   public void onMessage(NewOrderSingle message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
       OrdType orderType = message.getOrdType();
       Symbol currencyPair = message.getSymbol();
       /*
        * Assuming that we are directly dealing with Market
        */
       Price price = null;
       if (OrdType.FOREX_MARKET == orderType.getValue()) {
           if(this.priceMap.containsKey(currencyPair.getValue())){
               price = new Price(this.priceMap.get(currencyPair.getValue()));
           } else {
               price = new Price(1.4589);
           }
       } 
       //We are hardcoding this to 1, but in real world this may be something like a sequence.
       OrderID orderNumber = new OrderID(GenOrdID());
       //Id of the report, a unique identifier, once again this will be somthing like a sequence
       ExecID execId = new ExecID(GenExecID());
       //In this case this is a new order with no exception hence mark it as NEW
       ExecTransType exectutionTransactioType = new ExecTransType(ExecTransType.NEW);
       //This execution report is for confirming a new Order
       ExecType purposeOfExecutionReport =new ExecType(ExecType.FILL);
       //Represents status of the order, since this order was successful mark it as filled.
       OrdStatus orderStatus = new OrdStatus(OrdStatus.FILLED);
       //Represents the currencyPair
       Symbol symbol = currencyPair;
       //Represents side
       Side side = message.getSide();
       //What is the quantity left for the day, we will take 100 as a hardcoded value, we can also keep a note of this from say limit module.
       LeavesQty leavesQty = new LeavesQty(100);
       //Total quantity of all the trades booked in this applicaition, here it is hardcoded to be 100.
       CumQty cummulativeQuantity = new CumQty(100);
       //Average Price, say make it 1.235
       AvgPx avgPx = new AvgPx(1.235);
       ExecutionReport executionReport = new ExecutionReport(orderNumber,execId, purposeOfExecutionReport, orderStatus, side, leavesQty, cummulativeQuantity);
       executionReport.set(price);
       try {
           Session.sendToTarget(executionReport, sessionID);
       } catch (SessionNotFound e) {
           e.printStackTrace();
       }
   }
}