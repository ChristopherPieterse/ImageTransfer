package com.client;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.awt.event.ActionEvent;


public class Client {
	class DisplayPanel extends JPanel{ //creates a nice way to display an image
		private BufferedImage image;
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
			
		}
		public BufferedImage getImage() { //get it's current image
			return image;
		}
		public void setImage(BufferedImage image) { //set the image of the panel
			this.image = image;
			this.repaint();
		}
		
		
		
	}
	//streams
	private static InputStream is;
	private static OutputStream os;
	private static PrintWriter txtOut;
	private static BufferedReader txtin;
	private JFrame frame;
	
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();//create the gui
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	BufferedImage image;
	private BufferedImage readImage() { //read an image
		try {
			image = ImageIO.read(is);
			int extra = is.available();
			if(extra > 0) {
				byte[] buffer = new byte[extra];
				is.read(buffer);
				System.out.println(extra + " " + new String(buffer));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return image;
	}
	private void initialize() {
		//Gui elements
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JButton btnAddImage = new JButton("Add Image");
		
		btnAddImage.setBounds(10, 11, 89, 23);
		panel.add(btnAddImage);
		
		JButton btnGetImage = new JButton("Get Image");
		
		btnGetImage.setBounds(335, 11, 89, 23);
		panel.add(btnGetImage);
		
		JButton btnGetList = new JButton("Get List");
		btnGetList.addActionListener(new ActionListener() {//Get the list of images and returns them to the client
			public void actionPerformed(ActionEvent arg0) {
				Socket client = null;
				try {
					client = new Socket("localhost",7451);
					is = client.getInputStream();
					os = client.getOutputStream();
					txtOut = new PrintWriter(os);
					txtin =  new BufferedReader(new InputStreamReader(is));
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					txtOut.println("LIST");//sends the command to get the list
					txtOut.flush();
					String list = "";
					String listElement = txtin.readLine();
					while(txtin.ready()) { //reads through the list
						list = list + listElement + "\n";
						listElement = txtin.readLine();
					}
					JOptionPane.showMessageDialog(null, list);
				} catch (HeadlessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					client.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnGetList.setBounds(172, 11, 89, 23);
		panel.add(btnGetList);
		
		DisplayPanel imgPanel = new DisplayPanel();
		imgPanel.setBounds(10, 45, 414, 205);
		panel.add(imgPanel);
		imgPanel.setLayout(null);
		btnGetImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {//gets an image that is stored on the server
				Socket client = null;
				try {
					client = new Socket("localhost",7451);
					is = client.getInputStream();
					os = client.getOutputStream();
					txtOut = new PrintWriter(os);
					txtin =  new BufferedReader(new InputStreamReader(is));
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				String id = JOptionPane.showInputDialog("Choose image ID");
				txtOut.println("RET " + id);
				txtOut.flush();
				BufferedImage image = readImage();
				imgPanel.setImage(image);
				try {
					client.close();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
		btnAddImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {//prompts the user and adds the specified image to the server
				Socket client = null;
				try {
					client = new Socket("localhost",7451);
					is = client.getInputStream();
					os = client.getOutputStream();
					txtOut = new PrintWriter(os);
					txtin =  new BufferedReader(new InputStreamReader(is));
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String name;
				String id;
				String imageFilePath;
				BufferedImage image;
				name = JOptionPane.showInputDialog("What is the image name?");
				id = JOptionPane.showInputDialog("What is the new ID? Be sure to choose one that hasn't been chosen already.");
				imageFilePath = JOptionPane.showInputDialog("Input the full filename of the image you want to add to the server.");//VERY IMPORTANT. must be full filename
				try {
					txtOut.println("SEND " + id + " " + name);
					txtOut.flush();
					image = ImageIO.read(new File(imageFilePath));
					ByteArrayOutputStream arrByte = new ByteArrayOutputStream();//uses byte streams to convert the image to be able to be sent
					ImageIO.write(image, "jpg", arrByte);
					byte[] size = ByteBuffer.allocate(4).putInt(arrByte.size()).array();
					os.write(size);
					os.write(arrByte.toByteArray());
					os.flush();
					JOptionPane.showMessageDialog(null,txtin.readLine());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					client.close();
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
	}
}
