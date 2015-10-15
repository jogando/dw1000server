package serialPort;

public interface ISerialPortInterfaceObserver {
	public void receiveData(String data);
	public String getPortName();
}
