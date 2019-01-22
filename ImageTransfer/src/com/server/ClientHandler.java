package com.server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

public class ClientHandler implements Runnable {
	private Socket connection;
	private OutputStream os;
	private InputStream is;
	private PrintWriter txtout;
	private BufferedReader txtin;
	private BufferedReader filein;
	private BufferedWriter fileout;
	private File file;

	public ClientHandler(Socket connectionToClient) { //creates streams and accepts connections
		this.connection = connectionToClient;
		try {
			os = connection.getOutputStream();
			is = connection.getInputStream();
			txtin = new BufferedReader(new InputStreamReader(is));
			txtout = new PrintWriter(os);
			file = new File("img.txt");
			filein = new BufferedReader(new FileReader(file));
			fileout = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readFile() { //reads the text file contents
		String fileContents = "";

		try {
			String line = filein.readLine();
			while (line != null) {
				fileContents = fileContents  + line;
				line = filein.readLine();
				fileContents = fileContents  + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileContents;
	}
	public void close() { //closes streams
		try {
			os.close();
			is.close();
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private BufferedImage getImage(String id) { //get image from server file
		String filename = id+".jpg";
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File("data/server", filename));
			System.out.println("Image read");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String response = null;
		try {
			response = txtin.readLine();
			StringTokenizer tokens = new StringTokenizer(response);
			String resp = tokens.nextToken();
			String id;
			switch(resp) {
			case "RET": //if they want an image returned
				id = tokens.nextToken();
				BufferedImage image = getImage(id);
				ImageIO.write(image, "BMP", os);
				System.out.println("Image Sent");
				break;
			case "SEND": //if they are sending an image
				id = tokens.nextToken();
				String name = tokens.nextToken();
				byte[] arrByteSize = new byte[4];
				is.read(arrByteSize);
				int size = ByteBuffer.wrap(arrByteSize).asIntBuffer().get();
				byte[] arrByteImg = new byte[size];
				is.read(arrByteImg);
				BufferedImage buffImg = ImageIO.read(new ByteArrayInputStream(arrByteImg));
				File img = new File("data/server/"+id + ".jpg");
				boolean exists = img.createNewFile();
				if(exists)
				{
					FileWriter fileS = new FileWriter(file, true);
					BufferedWriter bw = new BufferedWriter(fileS);
					PrintWriter pr = new PrintWriter(bw);
					pr.println("\n" + id + " " + name);
					pr.flush();
					ImageIO.write(buffImg, "jpg" ,img);
					txtout.println("SUCCESS");
					txtout.flush();
				}else
				{
					System.out.println("Image already exists");
					txtout.println("FAILURE");
					txtout.flush();
				}
				break;
			case "LIST": //list all images
				String fileCont = readFile();
				txtout.println(fileCont);
				txtout.flush();
				break;
			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		close();
	}

}
