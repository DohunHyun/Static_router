package Router;

import java.util.Arrays;

/** 
 * 2������ �ش� �ϴ� Ethernet 
 * ���� ���� 
 *  1.IP �۽�
 *   -> ���� Layer�� ���� sendIP ȣ�� 
 *   -> encapsulation ( setSourceAddress, setDestinationAddress, setFrameType ) 
 *   -> PacketDriver layer�� send ȣ��
 *  2.ARP �۽�
 *   -> ���� Layer�� ���� sendARP ȣ�� 
 *   -> encapsulation ( setSourceAddress, setDestinationAddress, setFrameType )
 *     * ARP request or reply�� �˸��� destination address ����
 *   -> PacketDriver layer�� send ȣ��
 *  3.Frame ����
 *   -> ���� Layer�� ���� receive ȣ��
 *   -> Frame�� source & destination address Ȯ���Ͽ� ���� ����
 *    * �ڽ��� ���� �� X, Broadcast O, Ÿ���� ������ ���� �� O
 *   -> ���� �Ѵٸ�, decapsulation �� Ethernet type�� Ȯ���Ͽ� �˸��� ���� Lyaer�� ����
 *    * 0x0800 : IP Layer, 0x0806 : ARP Layer
 */

public class EthernetLayer extends BaseLayer {
	/* 2�������� ������ Frame�� �ִ� ũ�� �� ��� ������ �� �ʿ��� ��� ���� */
   final static int ETHERNET_MAX_SIZE = 1514;
   final static int ETHERNET_HEAD_SIZE = 14;
   final static int ETHERNET_MAX_DATA = ETHERNET_MAX_SIZE - ETHERNET_HEAD_SIZE;

   /* Frame�� ������ ��� ���� */
   byte[] Ethernet_type;
   byte[] Ethernet_sourceAddress;
   byte[] Ethernet_data;

   /* �� Layer������ ������ */
   int check = 0;

   public EthernetLayer(String layerName) {
      super(layerName);
      resetHeader();
   }

   /**
    * resetHeader : Frame�� ��� class ������ �ʱ�ȭ
    * @param  
    */
   void resetHeader() {
	  Ethernet_type = new byte[2];
      Ethernet_sourceAddress = new byte[6];
      Ethernet_data = new byte[ETHERNET_MAX_SIZE];
   }

   /**
    * setSourceAddress : �Ű������� ���� class ������ Ethernet_sourceAddress�� Ethernet_data�� �˸��� ��ġ�� ����
    * @param sourceAddress : �۽� �ϴ� device�� ������ �ּ�
    */
   void setSourceAddress(byte[] sourceAddress) {
      for (int i = 0; i < 6; i++) {
         Ethernet_sourceAddress[i] = sourceAddress[i];
         Ethernet_data[i + 6] = sourceAddress[i];
      }
   }

   /**
    * setDestinationAddress : �Ű������� ���� class ������ Ethernet_data�� �˸��� ��ġ�� ����
    * @param destinationAddress : ���� �ϴ� device�� ������ �ּ�
    */
   void setDestinationAddress(byte[] destinationAddress) {
      for (int i = 0; i < 6; i++)
         Ethernet_data[i] = destinationAddress[i];
   }
   
   /**
    * setFrameType : �Ű������� ���� class ������ Ethernet_data�� �˸��� ��ġ�� ����
    * @param frameType : Ethernet frame�� type, ip : 0x0800, arp : 0x0806
    */
   void setFrameType(byte[] frameType) {
      for (int i = 0; i < 2; i++)
         Ethernet_data[i + 12] = frameType[i];
   }

   /**
    * sendIP : IP Layer���� �� �����Ϳ� header�� encapsulation �Ͽ� ����
    * @param data : ������ data, packet
    * @param destinationAddress : ���� device ������ �ּ�
    */
   boolean sendIP(byte[] data, byte[] destinationAddress) {
      int length = data.length;
      byte[] type = { (byte) 0x08, 0x00 };							// Ip packet �̹Ƿ� type�� 0x0800
      Ethernet_data = new byte[data.length + ETHERNET_HEAD_SIZE];
      
      // header�� encapsulation, Ethernet type, source & destination device mac address
      setFrameType(type);											
      setSourceAddress(Ethernet_sourceAddress);
      setDestinationAddress(destinationAddress);

      // ���� �� data�� Ethernet frame���� ����
      for (int i = 0; i < length; i++)
         Ethernet_data[i + ETHERNET_HEAD_SIZE] = data[i];

      // ���� Layer�� ����, ������� Ethernet frame�� �� ����
      if (((PacketDriverLayer) this.getUnderLayer()).send(Ethernet_data, Ethernet_data.length))
         return true;
      else
         return false;
   }
   
   /**
    * sendARP : ARP Layer���� �� �����Ϳ� header�� encapsulation �Ͽ� ����
    * @param data : ������ data, ARP Packet
    */
   boolean sendARP(byte[] data) {
      int length = data.length;
      byte[] destinationAddress = new byte[6];
      Ethernet_data = new byte[data.length + ETHERNET_HEAD_SIZE];
      byte[] type = { 0x08, 0x06 };										// Arp packet �̹Ƿ� type�� 0x0806
      
      // header�� encapsulation, Ethernet type, source & destination device mac address
      setFrameType(type);
      setSourceAddress(Ethernet_sourceAddress);
      
      // encapsulation ���� ��, Arp ��Ŷ�� destination address�� ����
      // ARP ��Ŷ �� Operation Ȯ���Ͽ� request�� reply�� �����Ͽ� ����
      if (data[7] == 2) {			// ARP reply
    	 // ARP ��Ŷ�� target mac address�� ���� ����
         for (int i = 0; i < 6; i++)
            destinationAddress[i] = data[i + 18];
         setDestinationAddress(destinationAddress);
      } else {						// ARP request
    	  // BroadCast�� ����
         for (int i = 0; i < 6; i++)
            destinationAddress[i] = (byte) 0xff;
         setDestinationAddress(destinationAddress);
      }

      // ���� �� data�� Ethernet frame���� ����
      for (int i = 0; i < length; i++)
         Ethernet_data[i + ETHERNET_HEAD_SIZE] = data[i];

      // ���� Layer�� ����, ������� Ethernet frame�� �� ����
      if (((PacketDriverLayer) this.getUnderLayer()).send(Ethernet_data, Ethernet_data.length))
         return true;
      else
         return false;
   }

   /**
    * receive : PacketDriver Layer���� �� �����Ϳ� header�� Ǯ�� decapsulation �Ͽ� ����
    * @param data : ������ frame
    * synchronized : ���� ���̾�� thread �� receive ȣ��  �� ����ȭ�� ���Ͽ� ����
    */
   synchronized boolean receive(byte[] data) {
      byte[] destinationMAC = new byte[6];
      byte[] sourceMAC = new byte[6];
      byte[] broadcast = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
      
      // ������ frame���� ���� �ּ� ����, source & destination
      System.arraycopy(data, 0, destinationMAC, 0, 6);					
      System.arraycopy(data, 6, sourceMAC, 0, 6);
      
      // ��������� �ؾ��ϴ� frame���� �ּ� ��
 	  // �ڽ��� ���� frame���� üũ, �ƴϸ� ����
      if (java.util.Arrays.equals(Ethernet_sourceAddress, sourceMAC))
         return false;
      // Broadcast �ų� �ڽſ��� �� frame���� üũ, �´ٸ� ����
      if (!(java.util.Arrays.equals(broadcast, destinationMAC) || java.util.Arrays.equals(Ethernet_sourceAddress, destinationMAC)))
         return false;
      
      // ������ frame�� decapsulation�Ͽ� ���� ���̾�� ����
      byte[] dataFrame = new byte[data.length - ETHERNET_HEAD_SIZE];
      dataFrame = Arrays.copyOfRange(data, ETHERNET_HEAD_SIZE, data.length);
      // Ethernet frame type�� Ȯ���Ͽ�, �˸��� ���̾�� ����
      if (data[12] == 8 && data[13] == 0)	// IP : 0x0800
         ((IPLayer) this.getUpperLayer()).receiveIP(dataFrame);
      if (data[12] == 8 && data[13] == 6)	// ARP : 0x0806
         ((IPLayer) this.getUpperLayer()).receiveARP(dataFrame);
      return true;
   }
}