/* 
 * Authors: Mahesh V. Tripunitara and Ahmad R. Dhaini
 * Copyright (c) 2010, University of Waterloo
 *
 */
package ece428.socket;

import java.net.*;
import java.io.*;
import java.util.*;

public class T_DatagramSocket
{
	private DatagramSocket d_socket;
	private static int coin1 = 3;
	private static int coin2 = 2;
	private static int coin3 = 256;
	private static int maxBytesToMangle = 5;
	private static boolean DEBUG = true;
	private static boolean DEBUG_2 = true;

	private boolean invokeSRand = true;
	private Random generator;

	/* Constructor */
	/* Binds the socket to addr */
	public T_DatagramSocket(InetSocketAddress addr) throws SocketException
	{
        d_socket = new DatagramSocket(addr);	
		
		generator = new Random();		
	}

	/* Get details of socket */
	public InetSocketAddress T_getLocalSocketAddress() 
	{
		return(new InetSocketAddress(d_socket.getLocalAddress(), d_socket.getLocalPort()));
	}

	/* Send data */
	/* len is <= the space allocated for buf */
	public void T_sendto(byte buf[], int len, InetSocketAddress to) throws IOException
	{
		DatagramPacket p = new DatagramPacket(buf, len, to);

		if(DEBUG)
		{
		    System.out.println("T_send "+len);
		}
		
		if(invokeSRand)
		{
			generator = new Random();
			invokeSRand = false;
		}

	
		boolean doEvil = !(generator.nextInt(coin1) == 1) ? true : false; // 1 in coin1 chance of doing evil
		
		//doEvil = true;
	
		if(!doEvil)
		{
			/* Do no evil */	
		        if(DEBUG) {
			    System.out.println("Do no evil.");
			}
			
			d_socket.send(p);
			
			return;
		}

		/* What kind of evil? */
		boolean mangle = (generator.nextInt(coin2) == 1) ? true : false;

		//mangle = false;
		
		if(mangle)
		{
			/* Kind of evil: mangle */
			
		    if(DEBUG)
			{
			    System.out.println("Evil: mangle");
			}
					
			int numMangles = (generator.nextInt(maxBytesToMangle)) + 1;
			
			int threshholde = 440;
			for(; numMangles > 0 && len > threshholde; numMangles--) 
			{
			    int byteToMangle = threshholde + generator.nextInt(len - threshholde);

			   	Integer randMangle = new Integer(generator.nextInt(coin3));
				
				if(DEBUG)
				{
				    System.out.println("Mangle Rand = "+randMangle);
				}
				
			    buf[byteToMangle] = (byte)(randMangle.byteValue());	

			    if(DEBUG)
				{
					System.out.println("mangling ... "+Integer.toString(buf[byteToMangle] & 0xff, 16).toUpperCase());
			    }
			}
			
			/* Set the new data */
			p.setData(buf);

			/* Send Datagram */
			d_socket.send(p);
			
			return;
		}
		else
		{
			/* Kind of evil: drop */

			if(DEBUG_2)
			{
			    System.out.println("Evil: drop");
			}

			return;
		}
	}

	/* Set receive timeout */
	public void T_setSoTimeout(int timeout) throws SocketException
	{
		d_socket.setSoTimeout(timeout);
	}

	/* Receive data */
	/* len is the maximum bytes to be received. */
	/* The return DatagramPacket contains the identity of the sender,
	 * and the number of bytes actually received */
	public DatagramPacket T_recvfrom(int len) throws IOException
	{
		DatagramPacket p = new DatagramPacket(new byte[len], len);
		
		d_socket.receive(p);

		return(p);
	}

	/* Close Socket */
	public void T_close()
	{
		d_socket.close();	
	}	
}
