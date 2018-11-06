package com.easy.share.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

@SuppressWarnings("serial")
public class ShareEasyClient extends JFrame {

	private JPanel contentPane;
	private String serverIpAddress;
	private Socket socket;
	private JTextField jtfIp = new JTextField("192.168.2.49");
	private JLabel labelOutput = new JLabel("Output Window");
	private final JPanel panel = new JPanel();
	private final JButton jbtnBrowse = new JButton("Browse");
	private final JButton jbtnConnect = new JButton("Connect");
	private final JLabel lblBrowsePath = new JLabel("Browse Path");
	private String folderPath = new File(System.getProperty("user.home") + "/Downloads").getPath();;
	private StringBuilder receivedFilePaths = new StringBuilder();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShareEasyClient frame = new ShareEasyClient();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ShareEasyClient() {
		Container container = getContentPane();
		container.setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		contentPane.add(jtfIp, BorderLayout.BEFORE_FIRST_LINE);
		labelOutput.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(labelOutput, BorderLayout.CENTER);

		contentPane.add(panel, BorderLayout.WEST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 96, 0 };
		gbl_panel.rowHeights = new int[] { 29, 29, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		GridBagConstraints gbc_jbtnBrowse = new GridBagConstraints();
		gbc_jbtnBrowse.anchor = GridBagConstraints.WEST;
		gbc_jbtnBrowse.gridx = 0;
		gbc_jbtnBrowse.gridy = 4;
		jbtnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.home")));
				chooser.setDialogTitle("Select Location");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showSaveDialog(container) == JFileChooser.APPROVE_OPTION) {

					String path = chooser.getSelectedFile().getPath();
					int lastIndex = path.lastIndexOf("/") + 1;
					folderPath = path.substring(0, lastIndex);
					lblBrowsePath.setText("Browse Path : " + folderPath);
				}
			}
		});

		GridBagConstraints gbc_jbtnConnect = new GridBagConstraints();
		gbc_jbtnConnect.insets = new Insets(0, 0, 5, 0);
		gbc_jbtnConnect.anchor = GridBagConstraints.WEST;
		gbc_jbtnConnect.gridx = 0;
		gbc_jbtnConnect.gridy = 2;
		jbtnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				serverIpAddress = jtfIp.getText().toString();
				if (!serverIpAddress.equals("")) {
					Thread clientThread = new Thread(new ClientThread());
					clientThread.start();
				}
			}
		});
		panel.add(jbtnConnect, gbc_jbtnConnect);
		panel.add(jbtnBrowse, gbc_jbtnBrowse);
		lblBrowsePath.setHorizontalAlignment(SwingConstants.CENTER);

		contentPane.add(lblBrowsePath, BorderLayout.SOUTH);

	}

	public class ClientThread implements Runnable {
		public void run() {
			try {
				while (true) {
					InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
					socket = new Socket(serverAddr, 10000);

					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					
					String fileName = (String) ois.readObject();
					System.out.println(fileName);
					File file = new File(folderPath + fileName);
					byte[] content = (byte[]) ois.readObject();
					Files.write(file.toPath(), content);
					receivedFilePaths.append(folderPath+"\n");
					labelOutput.setText(receivedFilePaths.toString());
					ois.close();
					socket.close();
					Thread.sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
