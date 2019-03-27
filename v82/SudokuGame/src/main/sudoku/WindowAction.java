package main.sudoku;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowAction implements WindowListener {

	
	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		if((Game.gameStat==1||Game.gameStat==2)&& Game.PC.isGen && Game.PC.isBackupGen)
		{
			
			SaveLoad sl=new SaveLoad();
			sl.save(Game.PC,Game.gameStat);
		}
		System.out.println("Closing");
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		//SaveLoad sl=new SaveLoad();
		//sl.save();
		//System.exit(1);
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}
