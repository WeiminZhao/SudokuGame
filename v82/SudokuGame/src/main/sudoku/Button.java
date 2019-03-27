/**
 * 
 */
package main.sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * @author zwmsc
 *
 */
public class Button {
	
	
	int posx;
	int posy;
	int width;
	int height;
	int posEndx;
	int posEndy;
	
	boolean buttonFadein;
	//boolean textFadein;//and for now use button fadein control all fade in for this button
	//for now hover will always fade in
	int buttonFadeinRate=3;
	int hoverFadeinRate=3;
	 
	//control the fadein alpha value for button and its text
	int alphaForButton;
	int alphaForHover;
	int alphaForText;
	//String text;
	
	boolean hasMousePressed=false;
	boolean isMousePressing=false;
	
	
	public Button(int posx, int posy, int width, int height, boolean buttonFadein)
	{
		this.posx=posx;
		this.posy=posy;
		this.width=width;
		this.height=height;
		
		this.posEndx=posx+width;
		this.posEndy=posy+height;
		
		
		alphaForText=0;
		alphaForButton=0;
		alphaForHover=0;
		//if(textFadein)
		//this.textFadein=textFadein;
		
		//if(buttonFadein)
		this.buttonFadein=buttonFadein;
			
	}
	
	public void renderButton(Color c, Graphics g)//render only render the outline of button
	{
		Color colorForButton;
		if(buttonFadein && alphaForButton <= c.getAlpha()-buttonFadeinRate)//when fade in function is set to be true
			colorForButton=new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaForButton+=buttonFadeinRate);
		else
		{
			colorForButton=c;
			alphaForButton=c.getAlpha();
		}
		
		if(isMousePressing)//press effect
		{
			colorForButton=new Color(colorForButton.getRed(),colorForButton.getGreen(),colorForButton.getBlue(),colorForButton.getAlpha()/2);
			//alphaForButton=colorForButton.getAlpha();
		}
		
		g.setColor(colorForButton);
		g.drawRect(this.posx, this.posy, this.width, this.height);
	}
	
	public void renderHoverButton(int mousehx, int mousehy,Color c,Graphics g)//render only render the hover effect while mouse inside it
	{
		if(this.posx<=mousehx && mousehx<this.posEndx && this.posy<=mousehy && mousehy<this.posEndy)
		{
			Color colorForHover;
			
			if(alphaForHover<=c.getAlpha()-hoverFadeinRate && alphaForHover<=alphaForButton-hoverFadeinRate)//hover will always fade in
				colorForHover=new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaForHover+=hoverFadeinRate);//as hover's alpha
			else if(alphaForHover > c.getAlpha()-hoverFadeinRate && alphaForHover<=alphaForButton)
			{
				colorForHover=c;
				alphaForHover=c.getAlpha();
			}
			else //so now hover alpha will be always smaller than button itself's alpha
			{
				alphaForHover=alphaForButton;
				colorForHover=new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaForHover);
			}
				
			if(isMousePressing)//press effect
			{
				colorForHover=new Color(colorForHover.getRed(),colorForHover.getGreen(),colorForHover.getBlue(),colorForHover.getAlpha()/2);
				//alphaForButton=colorForButton.getAlpha();
			}
			
			g.setColor(colorForHover);
			g.fillRect(this.posx,this.posy,this.width,this.height);
		}
		else
		{
			alphaForHover=0;
		}
	}
	
	public boolean isMouseClickedIn(int mousex,int mousey,boolean isMouseDown)//will return true only on mouse completed both pressing and releasing on the same button
	{
		if(isMousePressingIn(mousex,mousey,isMouseDown))
		{
			this.hasMousePressed=true;
		}
		if(this.hasMousePressed && isMouseInside(mousex,mousey)&& !isMouseDown)//when release position==pressed position inside the button
		{
			resetAlphas();//this method will auto reset alpha when mouse clicked.
			this.hasMousePressed=false;
			return true;
		}
		else if(this.hasMousePressed && !isMouseInside(mousex,mousey))//&& !isMouseDown)//when release or went outside the button
			this.hasMousePressed=false;
		return false;
	}
	
	public boolean isMousePressingIn(int mousex, int mousey, boolean isMouseDown) //check if mouse is pressing the button//lets use this for checking click
	{
		if(isMouseDown && isMouseInside(mousex,mousey))
		{
			//resetAlphas();
			isMousePressing=true;
			return true;
		}
		isMousePressing=false;
		return false;
	}
	
	public boolean isMouseInside(int mousex, int mousey)//detect if given mouse location is inside it (and has pressed)
	{
		if(this.posx<=mousex && mousex<this.posEndx && this.posy<=mousey && mousey<this.posEndy)
			return true;
		return false;
	}
	
	public void setText(String text,Font f,int relativeX, int relativeY, Color c, Graphics g)//render, render the text related to the button
	{
		Color colorForText;
		if(buttonFadein && alphaForText <= c.getAlpha()-buttonFadeinRate)//when fade in function is set to be true
			colorForText=new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaForText+=buttonFadeinRate);
		else
		{
			colorForText=c;
			alphaForText=c.getAlpha();
		}
		
		if(isMousePressing)//press effect
		{
			colorForText=new Color(colorForText.getRed(),colorForText.getGreen(),colorForText.getBlue(),colorForText.getAlpha()/2);
			//alphaForText=colorForText.getAlpha();
		}
		
		g.setFont(f);
		g.setColor(colorForText);
		g.drawString(text, this.posx+relativeX,this.posy+relativeY);
	}
	public void setText(String text,Font f,boolean isCentered, Color c, Graphics g)
	{
		
		Color colorForText;
		if(buttonFadein && alphaForText <= c.getAlpha()-buttonFadeinRate)//when fade in function is set to be true
			colorForText=new Color(c.getRed(),c.getGreen(),c.getBlue(),alphaForText+=buttonFadeinRate);
		else
		{
			colorForText=c;
			alphaForText=c.getAlpha();
		}
		
		if(isMousePressing)//press effect
		{
			colorForText=new Color(colorForText.getRed(),colorForText.getGreen(),colorForText.getBlue(),colorForText.getAlpha()/2);
			//alphaForText=colorForText.getAlpha();
		}
		
		g.setFont(f);
		if(isCentered)
		{
			int fWidth = g.getFontMetrics().stringWidth(text);
			int fHeight = g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();//g.getFontMetrics().getHeight()-g.getFontMetrics().getLeading();//-g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();
			//g.getFontMetrics().;
			int relativeX=Math.abs(width - fWidth)/2; 
			int relativeY=Math.abs(height - fHeight)/2 ;
			
		
			
			
			g.setColor(colorForText);
			g.drawString(text, this.posx+relativeX,this.posy-relativeY+height);
		}
		else
		{
			int fHeight = g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent();
			int relativeY=Math.abs(height - fHeight)/2 ;
			
			g.setColor(colorForText);
			g.drawString(text, this.posx+5,this.posy-relativeY+height);
			
		}
	}
	
	public void buttonFill(int fillWidth, int fillHeight,Color c, Graphics g)//render, render the color filled inside the button
	{
		g.setColor(c);
		g.fillRect(this.posx, this.posy, fillWidth, fillHeight);
	}
	
	public void resetAlphas()//reset all alpha to zero for fade in
	{
		alphaForButton=0;
		alphaForText=0;
		alphaForHover=0;
	}
}
