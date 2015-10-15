package serialPort;

import java.util.ArrayList;
import java.util.List;

public class Dispatcher {
	private volatile static Dispatcher _instance;
	private List<ISerialPortInterfaceObserver> listObservers;
	private List<SerialPortInterface> listSerialPortInterfaces;
	
	private Dispatcher()
	{
		listSerialPortInterfaces = new ArrayList<SerialPortInterface>();
		listObservers = new ArrayList<ISerialPortInterfaceObserver>();
	}
	
	public synchronized void registerObserver(ISerialPortInterfaceObserver observer) throws Exception
	{
		boolean found = false;
		
		//first we need to check if we already have an interface open for that Serial Port
		for(SerialPortInterface spl : listSerialPortInterfaces)
		{
			if(spl.portName.equals(observer.getPortName()))
			{
				found = true;
				break;
			}
		}
		
		//If we didn't find any open connection for that Serial Port, we need to create it and initialize it
		if(!found)
		{
			SerialPortInterface spl = new SerialPortInterface(observer.getPortName());
			spl.initialize();
			listSerialPortInterfaces.add(spl);
		}
		
		listObservers.add(observer);
	}
	
	//send a String to a given Serial Port
	public synchronized void sendData(String portName, String data) throws Exception
	{
		for(SerialPortInterface spi : listSerialPortInterfaces)
		{
			if(spi.portName.equals(portName))
			{
				spi.sendData(data);
				break;
			}
		}
	}
	
	
	public static synchronized Dispatcher getInstance()
	{
		if(_instance == null)
        {
            _instance = new Dispatcher();
        }
        return _instance;
	}
	
	//When we read data from the serial port, we need to notify all the registered observers for that port name
	public synchronized void routeNewData(String portName, String data)
	{
		for(ISerialPortInterfaceObserver observer: listObservers)
		{
			if(observer.getPortName().equals(portName))
			{
				observer.receiveData(data);
			}
		}
	}
	
}
