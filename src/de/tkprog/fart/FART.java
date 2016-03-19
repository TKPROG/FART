package de.tkprog.fart;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class FART extends JFrame{

	public static void main(String[] args) {
		new FART();
	}

	private JList<String> list1;
	private JScrollPane scrollpane1;
	private JButton button1;
	private JButton button2;
	private JLabel label1;
	private JTextField textfield1;
	private JLabel label2;
	private JButton button3;
	private JButton button4;
	private JFileChooser fc;
	private FART THIS;
	ArrayList<File> files = new ArrayList<File>();
	private JFileChooser fc2;
	
	public FART(){
		THIS = this;
		setTitle("");
		setSize(500,400);
		setLocation(
				(int)(((double)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/(double)2)-((double)getWidth()/(double)2)),
				(int)(((double)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/(double)2)-((double)getHeight()/(double)2))
				);
		setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		label1 = new JLabel();
		label1.setText("Zu reparierende Dateien:");
		add(label1);
		
		list1 = new JList<String>();
		
		scrollpane1 = new JScrollPane(list1);
		add(scrollpane1);
		
		addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
				resize();
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
				resize();
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
				resize();
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				resize();
			}
		});
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		
		button1 = new JButton();
		button1.setText("+");
		button1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(fc.showOpenDialog(THIS)==JFileChooser.APPROVE_OPTION && fc.getSelectedFile() != null){
					for(File f : fc.getSelectedFiles()){
						files.add(f);
					}
				}
				fill();
			}
		});
		add(button1);
		
		button2 = new JButton();
		button2.setText("-");
		button2.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(list1.getSelectedIndex()!=-1){
					files.remove(list1.getSelectedIndex());
				}
				fill();
			}
		});
		add(button2);
		
		label2 = new JLabel();
		label2.setText("Speicherort: ");
		add(label2);
		
		textfield1 = new JTextField();
		add(textfield1);
		
		fc2 = new JFileChooser();
		fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		button3 = new JButton();
		button3.setText("...");
		button3.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(fc2.showSaveDialog(THIS)==JFileChooser.APPROVE_OPTION){
					textfield1.setText(fc2.getSelectedFile().getAbsolutePath());
				}
			}
		});
		add(button3);

		button4 = new JButton();
		button4.setText("Durchlaufen lassen!");
		button4.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				able(false);
				for(File f : files){
					try {
						parse(f,textfield1.getText());
					} catch (Exception e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(THIS, "Error occurred: "+e.getMessage());
					}
				}
				JOptionPane.showMessageDialog(THIS, "Alles abgearbeitet.");
				able(true);
			}
		});
		add(button4);
		
		able(true);
		resize();
		setVisible(true);
	}
	
	protected void parse(File f, String res_path) throws Exception {
		if(!f.exists()){
			throw new Exception("Die Datei \""+f.getName()+"\" existiert nicht.");
		}
		String d = readAll(f);
		if(d == null){
			throw new Exception("Datei gab null zur�ck.");
		}
		
		int pos = -1;
		while((pos = d.indexOf("alert"))>-1){
			int lastpos = check(d,pos);
			if(lastpos>-1&&lastpos>=pos){
				System.out.println(""+d.substring(pos, lastpos));
				d = d.substring(0, pos)+"true"+d.substring(lastpos, d.length());
			}
		}
		while((pos = d.indexOf("Muse.Assert.fail"))>-1){
			int lastpos = check(d,pos);
			if(lastpos>-1&&lastpos>=pos){
				System.out.println(""+d.substring(pos, lastpos));
				d = d.substring(0, pos)+"true"+d.substring(lastpos, d.length());
			}
		}
		
		File ff = new File(new File(res_path).getAbsolutePath()+"/"+f.getName());
		if(ff.exists()){
			if(JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(THIS, "Die Datei \""+ff.getName()+"\" existiert bereits. M�chten Sie diese Datei �berschreiben?", "�berschreiben...?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)){
				return;
			}
		}
		BufferedWriter bf = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ff),"UTF8"));
		bf.write(d);
		bf.flush();
		bf.close();
		bf = null;
	}

	private int check(String d, int pos) {
		int openBrackets = 0;
		int openQuotationMark = 0;
		for(int a = pos;a < d.length();a++){
			switch(d.charAt(a)){
			case '(':
				openQuotationMark++;
				break;
			case ')':
				openQuotationMark--;
				if(openQuotationMark==0){
					return (a+1);
				}
				break;
			case '"':
				break;
				default:
			}
		}
		return -1;
	}

	private String readAll(File f) throws Exception{
		InputStreamReader isr = new InputStreamReader(new FileInputStream(f),"UTF8");
		String d = "";
		int dd = -1;
		while((dd=isr.read())!=-1){
			d+=String.valueOf((char)dd);
		}
		isr.close();
		isr = null;
		return d;
	}

	private void fill(){
		String[] data = new String[files.size()];
		for(int a = 0;a < files.size();a++){
			data[a] = files.get(a).getAbsolutePath();
		}
		list1.setListData(data);
		resize();
	}
	
	private void able(boolean d){
		list1.setEnabled(d);
		scrollpane1.setEnabled(d);
		button1.setEnabled(d);
		button2.setEnabled(d);
		label1.setEnabled(d);
		textfield1.setEnabled(d);
		label2.setEnabled(d);
		button3.setEnabled(d);
		button4.setEnabled(d);
	}
	
	private void resize(){
		label1.setBounds(10, 10, (int)label1.getPreferredSize().getWidth(), (int)label1.getPreferredSize().getHeight());
		button1.setBounds(getWidth()-(int)button1.getPreferredSize().getWidth()-10-13, label1.getY()+label1.getHeight()+10, (int)button1.getPreferredSize().getWidth(), (int)button1.getPreferredSize().getHeight());
		button2.setBounds(button1.getX(), button1.getY()+button1.getHeight()+10, button1.getWidth(), button1.getHeight());

		button4.setBounds(label1.getX(), getHeight()-10-(int)button4.getPreferredSize().getHeight()-35, (int)(getWidth()-(2.0d*(double)label1.getX()))-15, (int)button4.getPreferredSize().getHeight());
		
		label2.setBounds(label1.getX(), button4.getY()-15-(int)label2.getPreferredSize().getHeight(), (int)label2.getPreferredSize().getWidth(), (int)label2.getPreferredSize().getHeight());
		button3.setBounds(button1.getX()+button1.getWidth()-(int)button3.getPreferredSize().getWidth(), label2.getY(), (int)button3.getPreferredSize().getWidth(), (int)button3.getPreferredSize().getHeight());
		textfield1.setBounds(label2.getX()+label2.getWidth()+10, label2.getY(), button3.getX()-10-10-label2.getWidth()-label2.getX(),(int)textfield1.getPreferredSize().getHeight());
		
		scrollpane1.setBounds(label1.getX(), label1.getY()+label1.getHeight()+10, button1.getX()-10-10, label2.getY()-10-10-label1.getY()-label1.getHeight());
	}

}
