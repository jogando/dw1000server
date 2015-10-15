package serialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


import gnu.io.*;

public class SerialPortInterface implements SerialPortEventListener {
	public String portName;
	public SerialPortInterface(String _portName)
	{
		portName = _portName;
	}
	
	SerialPort serialPort;
    
    private BufferedReader input;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 115200; //baud rate configured in the Arduino
    
    public void initialize() throws Exception {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
            }
        }
        
        if (portId == null) {
            throw new Exception ("Could not find COM port.");
        }

        // open serial port, and use class name for the appName.
        serialPort = (SerialPort) portId.open(this.getClass().getName(),
            TIME_OUT);

        // set port parameters
        serialPort.setSerialPortParams(DATA_RATE,
            SerialPort.DATABITS_8,
            SerialPort.STOPBITS_1,
            SerialPort.PARITY_NONE);

        // open the streams
        input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        output = serialPort.getOutputStream();

        // add event listeners
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);
    }
    
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public synchronized void sendData(String data) throws Exception
    {
    	output.write(data.getBytes());
    }
    
    @Override
    public void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            
            try {
                String inputLine=input.readLine();
                //System.out.println(portName+": "+inputLine);
                //we only care for messages with the following format: "[<message>]"
                if(inputLine.startsWith("[") && inputLine.endsWith("]"))
                {
                	//We notify the Dispatcher the new data received.
                    Dispatcher.getInstance().routeNewData(portName, inputLine);
                    //System.out.println(inputLine);
                }
                else
                {
                    //System.out.println("data not understood.");
                }
            } catch (Exception ex) {
               //System.err.println(e.toString());
            	ex.printStackTrace();
            }
        }
    }
	
}
