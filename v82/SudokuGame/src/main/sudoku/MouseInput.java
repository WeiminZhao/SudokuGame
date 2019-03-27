package main.sudoku;

import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseInput implements MouseListener{
	
	//private Player player = null;

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		// TODO Auto-generated method stub
		int mx = e.getX();
		int my = e.getY();
		/*
		 * 	public Rectangle playButton = new Rectangle(Game.WIDTH / 2 + 120, 150,100,50);
			public Rectangle helpButton = new Rectangle(Game.WIDTH / 2 + 120, 250,100,50);
			public Rectangle quitButton = new Rectangle(Game.WIDTH / 2 + 120, 350,100,50);
	
		 */
		//the map box from 20 to 380
		
		Game.isMouseDown=true;
		Game.mousex=mx;
		Game.mousey=my;
		
		System.out.println(mx+"  "+my+" isMouseDown: "+Game.isMouseDown);
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		int mx = e.getX();
		int my = e.getY();
		
		Game.isMouseDown=false;
		Game.mousex=mx;
		Game.mousey=my;
		
		System.out.println("        isMouseDown: "+Game.isMouseDown);
	}
	
	

}
