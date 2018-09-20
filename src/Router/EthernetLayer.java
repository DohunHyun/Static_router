package Router;

import java.util.Arrays;

public class EthernetLayer extends BaseLayer {
   final static int ETHERNET_MAX_SIZE = 1514;
   final static int ETHERNET_HEAD_SIZE = 14;
   final static int ETHERNET_MAX_DATA = ETHERNET_MAX_SIZE - ETHERNET_HEAD_SIZE;

   byte[] Ethernet_type;
   byte[] Ethernet_sourceAddress;
   byte[] Ethernet_data;

   int check = 0;

   public EthernetLayer(String layerName) {
      super(layerName);
      resetHeader();
   }

   void resetHeader() {
      Ethernet_type = new byte[2];
      Ethernet_sourceAddress = new byte[6];
      Ethernet_data = new byte[ETHERNET_MAX_SIZE];
   }

   void setSourceAddress(byte[] sourceAddress) {
      for (int i = 0; i < 6; i++) {
         Ethernet_sourceAddress[i] = sourceAddress[i];
         Ethernet_data[i + 6] = sourceAddress[i];
      }
   }

   void setDestinationAddress(byte[] destinationAddress) {
      for (int i = 0; i < 6; i++)
         Ethernet_data[i] = destinationAddress[i];
   }

   void setFrameType(byte[] frameType) {
      for (int i = 0; i < 2; i++)
         Ethernet_data[i + 12] = frameType[i];
   }

   boolean sendIP(byte[] data, byte[] destinationAddress) {
      int length = data.length;
      byte[] type = { (byte) 0x08, 0x00 };
      Ethernet_data = new byte[data.length + ETHERNET_HEAD_SIZE];
      setFrameType(type);
      setSourceAddress(Ethernet_sourceAddress);
      setDestinationAddress(destinationAddress);

      for (int i = 0; i < length; i++)
         Ethernet_data[i + ETHERNET_HEAD_SIZE] = data[i];

      if (((PacketDriverLayer) this.getUnderLayer()).send(Ethernet_data, Ethernet_data.length))
         return true;
      else
         return false;
   }

   boolean sendARP(byte[] data) {
      int length = data.length;
      byte[] destinationAddress = new byte[6];
      Ethernet_data = new byte[data.length + ETHERNET_HEAD_SIZE];
      byte[] type = { 0x08, 0x06 };
      setFrameType(type);
      setSourceAddress(Ethernet_sourceAddress);

      if (data[7] == 2) {
         for (int i = 0; i < 6; i++)
            destinationAddress[i] = data[i + 18];
         setDestinationAddress(destinationAddress);
      } else {
         for (int i = 0; i < 6; i++)
            destinationAddress[i] = (byte) 0xff;
         setDestinationAddress(destinationAddress);
      }

      for (int i = 0; i < length; i++)
         Ethernet_data[i + ETHERNET_HEAD_SIZE] = data[i];

      if (((PacketDriverLayer) this.getUnderLayer()).send(Ethernet_data, Ethernet_data.length))
         return true;
      else
         return false;
   }

 
   synchronized boolean receive(byte[] data) {
      byte[] destinationMAC = new byte[6];
      byte[] sourceMAC = new byte[6];
      byte[] broadcast = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
      System.arraycopy(data, 0, destinationMAC, 0, 6);
      System.arraycopy(data, 6, sourceMAC, 0, 6);
      
      if (java.util.Arrays.equals(Ethernet_sourceAddress, sourceMAC))
         return false;
      if (!(java.util.Arrays.equals(broadcast, destinationMAC) || java.util.Arrays.equals(Ethernet_sourceAddress, destinationMAC)))
         return false;

      byte[] dataFrame = new byte[data.length - ETHERNET_HEAD_SIZE];
      dataFrame = Arrays.copyOfRange(data, ETHERNET_HEAD_SIZE, data.length);
      if (data[12] == 8 && data[13] == 0)
         ((IPLayer) this.getUpperLayer()).receiveIP(dataFrame);
      if (data[12] == 8 && data[13] == 6)
         ((IPLayer) this.getUpperLayer()).receiveARP(dataFrame);
      return true;
   }
}