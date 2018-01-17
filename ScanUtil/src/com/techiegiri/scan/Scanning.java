package com.techiegiri.scan;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerDevice;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.twain.TwainSource;

public class Scanning extends JApplet implements PlugIn, ScannerListener {

	private JToolBar jtoolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
	ImagePanel ipanel;
	Image im = null;
	BufferedImage imageforCrop;
	ImagePlus imp = null;
	int imageWidth;
	int imageHeight;
	private static final long serialVersionUID = 1L;
	Container content = null;
	private JPanel jContentPane = null;
	private JButton jButton = null;
	private JButton jButton1 = null;
	JCheckBox clipBox = null;
	JPanel crpdpanel = null;
	JPanel cpanel = null;
	private Scanner scanner = null;
	TwainSource ts;

	ImagePanel imagePanel, imagePanel2;

	public static void main(String[] args) {
		System.out.println("Inside main");
		new Scanning().setVisible(true);
	}

	public void run(String arg0) {
		System.out.println("Inside run");
		new Scanning().setVisible(false);
		repaint();
	}

	/**
	 * This is the default constructor
	 */
	public Scanning() {

		super();
		System.out.println("Inside constructor");
		init();
		try {
			scanner = Scanner.getDevice();
			scanner.addListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	public void init() {
		System.out.println("Inside init");
		this.setSize(1200, 600);
		this.setLayout(null);
		// this.revalidate();
		this.setContentPane(getJContentPane());
	}

	public void stop() {

	}

	public void destroy() {

	}

	public void start() {

	}

	private JToolBar getJToolBar() {
		System.out.println("Inside getJToolBar");
		jtoolbar.add(getJButton1());
		jtoolbar.add(getJButton());

		jtoolbar.setName("My Toolbar");
		jtoolbar.addSeparator();
		Rectangle r = new Rectangle(0, 0, 1024, 30);
		jtoolbar.setBounds(r);
		jtoolbar.setBackground(Color.white);
		return jtoolbar;
	}

	private JPanel getJContentPane() {
		System.out.println("Inside getJContentPane 1 ");
		if (jContentPane == null) {
			System.out.println("Inside getJContentPane null ");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.setBackground(Color.white);
			jContentPane.add(getJToolBar());
		}
		return jContentPane;
	}

	private JButton getJButton() {
		System.out.println("Inside getJButton 1 ");
		if (jButton == null) {
			System.out.println("Inside getJButton null");
			jButton = new JButton();
			jButton.setBounds(new Rectangle(4, 16, 131, 42));
			jButton.setText("Select Device");
			jButton.setBackground(Color.white);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out
							.println("Inside getJButton action actionPerformed ");
					if (scanner.isBusy() == false) {
						selectDevice();
					}

				}
			});
		}
		return jButton;
	}

	/* Select the twain source! */
	public void selectDevice() {

		try {
			System.out.println("Inside selectDevice");
			scanner.select();
		} catch (ScannerIOException e1) {
			IJ.error(e1.toString());
		}

	}

	private JButton getJButton1() {
		System.out.println("Inside getJButton1");
		if (jButton1 == null) {
			System.out.println("Inside getJButton null");
			jButton1 = new JButton();
			jButton1.setBounds(new Rectangle(35, 0, 30, 30));
			jButton1.setText("Scan");
			jButton1.setBackground(Color.white);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Inside getJButton1 actionPerformed");
					getScan();
				}
			});
		}
		return jButton1;
	}

	public void getScan() {

		try {
			System.out.println("Inside getScan");
			scanner.acquire();
		} catch (ScannerIOException e1) {
			IJ.showMessage("Access denied! \nTwain dialog maybe already opened!");
			e1.printStackTrace();
		}
	}

	public Image getImage() {
		System.out.println("Inside getImage");
		Image image = imp.getImage();
		return image;
	}

	public void update(ScannerIOMetadata.Type type, ScannerIOMetadata metadata) {

		System.out.println("Inside update");
		if (type.equals(ScannerIOMetadata.ACQUIRED)) {
			System.out.println("Inside update2");

			if (imp != null) {
				System.out.println("Inside update3");
				jContentPane.remove(ipanel);
				jContentPane.remove(cpanel);
				jContentPane.remove(crpdpanel);
			}

			imp = new ImagePlus("Scan", metadata.getImage());

			im = imp.getImage();

			imagePanel = new ImagePanel(im);
			imagePanel.updateUI();

			imagePanel.repaint();
			imagePanel.revalidate();

			ClipMover mover = new ClipMover(imagePanel);
			imagePanel.addMouseListener(mover);
			imagePanel.addMouseMotionListener(mover);

			ipanel = imagePanel.getPanel();

			ipanel.setBorder(new LineBorder(Color.blue, 1));
			ipanel.setBorder(BorderFactory.createTitledBorder("Scanned Image"));
			ipanel.setBounds(0, 30, 600, 600);
			ipanel.repaint();
			ipanel.revalidate();
			ipanel.updateUI();
			jContentPane.add(ipanel);
			jContentPane.getRootPane().revalidate();
			jContentPane.updateUI();

			cpanel = imagePanel.getUIPanel();
			cpanel.setBounds(700, 30, 300, 150);
			cpanel.repaint();
			cpanel.setBorder(new LineBorder(Color.blue, 1));
			cpanel.setBorder(BorderFactory.createTitledBorder("Cropping Image"));
			cpanel.setBackground(Color.white);
			jContentPane.add(cpanel);

			jContentPane.repaint();
			jContentPane.revalidate();

			metadata.setImage(null);
			try {
				new uk.co.mmscomputing.concurrent.Semaphore(0, true)
						.tryAcquire(2000, null);
			} catch (InterruptedException e) {
				IJ.error(e.getMessage());

			}

		}

		else if (type.equals(ScannerIOMetadata.NEGOTIATE)) {
			ScannerDevice device = metadata.getDevice();
			try {
				device.setResolution(100);
			} catch (ScannerIOException e) {
				IJ.error(e.getMessage());
			}

			try {

				device.setShowUserInterface(true);
				device.setResolution(100);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		else if (type.equals(ScannerIOMetadata.STATECHANGE)) {
			System.out.println("Scanner State " + metadata.getStateStr());
			System.out.println("Scanner State " + metadata.getState());

			if ((metadata.getLastState() == 3) && (metadata.getState() == 4)) {
			}

		} else if (type.equals(ScannerIOMetadata.EXCEPTION)) {
			IJ.error(metadata.getException().toString());

		}

	}

	class ImagePanel extends JPanel {
		private Image image;
		Image cimg;
		int imageWidth;
		int imageHeight;

		BufferedImage imageb;
		Dimension size;
		public Rectangle clip;
		boolean showClip, clipedImg;

		public boolean isShowClip() {
			return showClip;
		}

		JSlider slider1 = new JSlider(SwingConstants.HORIZONTAL, 80, 180, 80);

		ClipedPanel clipedPanel;

		ImagePanel(Image image) {
			this.image = image;
			this.imageWidth = image.getWidth(null);
			this.imageHeight = image.getHeight(null);
			this.imageb = (BufferedImage) image;
			size = new Dimension(imageb.getWidth(), imageb.getHeight());
			showClip = true;

		}

		public Image getImage() {
			return image;
		}

		public Image getCimg() {
			return cimg;
		}

		protected void paintComponent(Graphics g) {
			System.out.println("Inside paintComponent");
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int x = (getWidth() - size.width) / 2;
			int y = (getHeight() - size.height) / 2;

			g2.drawImage(imageb, x, y, this);
			if (showClip) {
				if (clip == null)
					createClip(80, 80);
				g2.setPaint(Color.red);
				g2.draw(clip);
			}
		}

		public void setClip(int x, int y) {

			System.out.println("Inside setClip");
			int x0 = (getWidth() - size.width) / 2;
			int y0 = (getHeight() - size.height) / 2;

			if (x < x0 || x + clip.width > x0 + size.width || y < y0
					|| y + clip.height > y0 + size.height)
				return;

			clip.setLocation(x, y);
			repaint();

			clipImage();
			repaint();
		}

		public Dimension getPreferredSize() {
			return size;
		}

		private void createClip(int sx, int sy) {
			System.out.println("Inside createClip");
			clip = new Rectangle(sx, sy);

			clip.x = (getWidth() - clip.width) / 2;
			clip.y = (getHeight() - clip.height) / 2;
		}

		public JPanel getCroppedPanel() {
			System.out.println("Inside getCroppedPanel");

			ClipedPanel cpanel = this.getClippedImg();

			return cpanel;
		}

		private void clipImage()

		{
			System.out.println("Inside clipImage");
			BufferedImage clipped = null;
			try {
				int w = clip.width;
				int h = clip.height;
				int x0 = (getWidth() - size.width) / 2;
				int y0 = (getHeight() - size.height) / 2;
				int x = clip.x - x0;
				int y = clip.y - y0;
				clipped = imageb.getSubimage(x, y, w, h);

				cimg = clipped;

				clipedPanel = new ClipedPanel(cimg);

				crpdpanel = imagePanel.getCroppedPanel();

				crpdpanel.setBounds(700, 200, 300, 300);
				crpdpanel.repaint();

				crpdpanel.setBorder(new LineBorder(Color.red, 1));
				crpdpanel.setBorder(BorderFactory
						.createTitledBorder("Cropped Image"));
				crpdpanel.setBackground(Color.white);

				jContentPane.add(crpdpanel);
				jContentPane.repaint();
				jContentPane.revalidate();

			} catch (RasterFormatException rfe) {
				System.out.println("raster format error: " + rfe.getMessage());
			}

		}

		public ClipedPanel getClippedImg() {
			return clipedPanel;
		}

		public JPanel getUIPanel() {
			System.out.println("Inside getUIPanel");

			JButton clip = new JButton("Crop image");
			clip.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Inside actionPerformed");
					repaint();
					if (getClippedImg() != null)
						jContentPane.remove(getClippedImg());
					if (showClip) {
						clipImage();
						repaint();
						clipedImg = true;

					} else {
						JOptionPane.showMessageDialog(null,
								"First Check Show clip Check Box!");
					}
				}
			});

			slider1.setPaintLabels(true);
			slider1.setPaintTicks(true);

			slider1.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					repaint();

					int sx1 = slider1.getValue();
					int sy1 = slider1.getValue();
					createClip(sx1, sy1);

					repaint();
					if (getClippedImg() != null)
						jContentPane.remove(getClippedImg());

					if (isShowClip()) {
						clipImage();
						repaint();
					} else {
						JOptionPane.showMessageDialog(null,
								"First Check Show clip Check Box!");
					}

				}
			});

			JButton save = new JButton("Save image");
			JButton upload = new JButton("Upload image");
			JButton saveDoc = new JButton("Save Document (Without Cropping)");

			upload.addActionListener(new ActionListener()

			{

				public void actionPerformed(ActionEvent e) {
					System.out.println("Inside actionPerformed upload");
					if (clipedImg) {
						// saveImg();
						repaint();
						if (getClippedImg() != null)
							jContentPane.remove(getClippedImg());
					}

					Image image = getCimg();

					BufferedImage bufferedImage = new BufferedImage(image
							.getWidth(null), image.getHeight(null),
							BufferedImage.TYPE_INT_RGB);
					bufferedImage.createGraphics().drawImage(image, 0, 0, null);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					try {
						ImageIO.write(bufferedImage, "png", baos);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					InputStream is = new ByteArrayInputStream(baos
							.toByteArray());
					JOptionPane.showMessageDialog(null,
							"Server Settings need to be Configured.",
							"Upload Image", 1);

				}
			}

			);

			save.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Inside actionPerformed add");
					if (clipedImg) {

						repaint();
						if (getClippedImg() != null)
							jContentPane.remove(getClippedImg());
					}

					saveImg();

				}
			});

			saveDoc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Inside actionPerformed save");
					repaint();

					saveDoc();
					repaint();

				}

			});

			JPanel panel = new JPanel();
			// panel.add(clipBox);
			panel.add(clip);
			panel.add(slider1);
			slider1.setBackground(Color.white);
			panel.add(save);
			panel.add(upload);
			panel.add(saveDoc);
			panel.revalidate();
			return panel;
		}

		public void saveImg() {

			System.out.println("Inside actionPerformed saveImg");
			Image image = getCimg();

			BufferedImage bufferedImage = new BufferedImage(
					image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_INT_RGB);
			bufferedImage.createGraphics().drawImage(image, 0, 0, null);
			JFileChooser chooser = new JFileChooser();

			String e[] = ImageIO.getWriterFormatNames();
			for (int i = 0; i < e.length; i++)
				chooser.addChoosableFileFilter(null);
			int result = chooser.showSaveDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {

				String ext = "JPG";
				File file = chooser.getSelectedFile();
				String name = file.getName();
				if (!name.endsWith(ext))
					file = new File(file.getParentFile(), name + "." + ext);
				try {
					ImageIO.write(bufferedImage, ext, file);

				}

				catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}

		public void saveDoc() {
			System.out.println("Inside actionPerformed saveDoc");
			Image image = getImage();

			BufferedImage bufferedImage = new BufferedImage(
					image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_BYTE_GRAY);
			bufferedImage.createGraphics().drawImage(image, 0, 0, null);
			JFileChooser chooser = new JFileChooser();

			String e[] = ImageIO.getWriterFormatNames();
			for (int i = 0; i < e.length; i++)
				chooser.addChoosableFileFilter(null);
			int result = chooser.showSaveDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {

				String ext = "JPG";

				File file = chooser.getSelectedFile();
				String name = file.getName();
				if (!name.endsWith(ext))
					file = new File(file.getParentFile(), name + "." + ext);
				try {
					ImageIO.write(bufferedImage, ext, file);

				}

				catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}

		public ImagePanel getPanel() {
			return this;
		}
	}

	class ClipMover extends MouseInputAdapter {
		ImagePanel cropping;
		Point offset;
		boolean dragging;

		public ClipMover(ImagePanel c) {
			cropping = c;
			offset = new Point();
			dragging = false;
		}

		public void mousePressed(MouseEvent e) {
			System.out.println("Inside mousePressed");
			Point p = e.getPoint();

			if (cropping.clip.contains(p)) {
				offset.x = p.x - cropping.clip.x;
				offset.y = p.y - cropping.clip.y;
				dragging = true;
			}
		}

		public void mouseReleased(MouseEvent e) {
			System.out.println("Inside mouse released");
			dragging = false;
		}

		public void mouseDragged(MouseEvent e) {

			if (dragging) {
				System.out.println("Inside mouse dragged");
				ClipedPanel xcpanel = cropping.getClippedImg();

				if (xcpanel != null) {
					jContentPane.remove(xcpanel);
				}

				int x = e.getX() - offset.x;
				int y = e.getY() - offset.y;

				if (cropping.isShowClip())
					cropping.setClip(x, y);

			}
		}

	}

}

class ClipedPanel extends JPanel {
	private Image image;
	int imageWidth;
	int imageHeight;

	BufferedImage imageb;
	Dimension size;
	Rectangle clip;
	boolean showClip;

	ClipedPanel(Image image) {
		this.image = image;
		this.imageWidth = image.getWidth(null);
		this.imageHeight = image.getHeight(null);

		this.imageb = (BufferedImage) image;
		size = new Dimension(imageb.getWidth(), imageb.getHeight());
		showClip = false;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		System.out.println("Inside paintComponent");
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int x = (getWidth() - size.width) / 2;
		int y = (getHeight() - size.height) / 2;

		g2.drawImage(imageb, x, y, this);

	}

	public ClipedPanel getPanel() {
		return this;
	}
}
