package framework;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import framework.imageFilters.ImageFilter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

/**
 * Custom Image class, based off a BufferedImage and a Shape. 
 */
public class Image {
	/** Shape used to generate the BufferedImage **/
	private Shape shape;
	/** Shape used when the first Image was created, before any rotations. **/
	private Shape ogShape;
	/** The actual image **/
	private BufferedImage image;
	/** Functions that are run over the BufferedImage, saved here to be used for copied or rotated Images **/
	private List<ImageFilter> filters;
	/** Base color to fill the shape with **/
	private Color color;
	/** Level that the Image is associated with **/
	private Level level;
	
	/** Width of the BufferedImage **/
	private int width;
	/** Height of the BufferedImage **/
	private int height;
	/** X position to draw image **/
	private double x;
	/** Y position to draw image **/
	private double y;
	/** Degrees the shape has been rotated from its original position **/
	private double rotation = 0;
	/** Number of Image objects that currently exist in the program **/
	private static int imageCount = 0;

	/**
	 * Create an image by filling in the provided shape with the provided color
	 * @param shape Shape that will be filled in. Can be any class that implements the Shape interface 
	 * @param color Color that the the shape will be filled with
	 * @param level Level that the Image is associated with
	 */
	public Image(Shape shape, Color color, Level level)
	{
		imageCount = getImageCount() + 1;
		this.level = level;
		this.x = 0;
		this.y = 0;
		this.shape = shape;
		this.ogShape = shape;
		double scale = level.getManager().getScale();
		AffineTransform transform = new AffineTransform();
		transform.scale(scale, scale);
		Shape scaled = transform.createTransformedShape(this.shape);
		this.width = (int)Math.ceil(scaled.getBounds2D().getWidth());
		this.height = (int)Math.ceil(scaled.getBounds2D().getHeight());
		this.image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
		this.color = color;
		this.filters = new ArrayList<ImageFilter>();
		for(int x = 0; x < width; x++) {
		    for(int y = 0; y < height; y++) {
		    	if (scaled.contains(new Point(x, y)))
		    	{
		    		image.setRGB(x, y, this.color.getRGB());
		    	}
		    	else
		    	{
		    		image.setRGB(x, y, Color.TRANSLUCENT);
		    	}
		    }
		}
	}
	/**
	 * Private constructor with the bare minimum of info needed to make a BufferedImage. Used when making a copy.
	 * @param width Width of new image
	 * @param height Height of new image
	 */
	private Image(int width, int height) {
		this.width = width;
		this.height = height;
		this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	/**
	 * Makes a copy of the Image, including all info such as shape and filters
	 * @return A new Image object
	 */
	public Image copy()
	{
		Image newImage = new Image(this.width, this.height);
		newImage.color = this.color;
		newImage.x = this.x;
		newImage.y = this.y;
		newImage.rotation = this.rotation;
		newImage.level = this.level;
		for (int x = 0; x < width; x++) 
		{
			for(int y = 0; y < height; y++) {
				newImage.image.setRGB(x, y, this.image.getRGB(x, y));
			}
		}
		newImage.filters = new ArrayList<ImageFilter>();
		for (int i = 0; i < this.filters.size(); i++)
		{
			newImage.addFilter(this.filters.get(i));
		}
		newImage.shape = this.shape;
		return newImage;
	}
	/**
	 * Rotate the image. This procedure does not rotate the BufferedImage, but makes a new Image with a rotated shape.
	 * @param angle Angle in degrees to rotate
	 */
	public void rotate(double angle) {
		this.image = null;
		this.rotation += Math.toRadians(angle);
		double scale = level.getManager().getScale();
		
		AffineTransform transform = new AffineTransform();
		transform.rotate(this.rotation, this.x + this.ogShape.getBounds2D().getCenterX(), 
				this.y + this.ogShape.getBounds2D().getCenterY());
		Shape newShape = transform.createTransformedShape(this.ogShape);
		
		transform = new AffineTransform();
		// Rotated shape ends up offset for some reason. I have no idea why I have to do this.
		transform.translate(-newShape.getBounds2D().getX(), -newShape.getBounds2D().getY());
		newShape = transform.createTransformedShape(newShape);
		Image rotated = new Image(newShape, this.color, this.level);
		
		this.shape = ogShape;
		this.image = rotated.getImage();
		this.width = (int)Math.ceil(rotated.getShape().getBounds2D().getWidth() * scale);
		this.height = (int)Math.ceil(rotated.getShape().getBounds2D().getHeight() * scale);
		rotated.filters = new ArrayList<ImageFilter>();
		for (int i = 0; i < this.filters.size(); i++)
		{
			rotated.addFilter(this.filters.get(i));
		}
		this.x = rotated.getX();
		this.y = rotated.getY();
	}
	/** 
	 * Flip the image around the Y axis
	 */
	public void flipY()
	{
	    for (int x=0; x < image.getWidth(); x++)
	    {
	        for (int y=0; y < image.getHeight() / 2; y++)
	        {
	            int tmp = image.getRGB(x, y);
	            image.setRGB(x, y, image.getRGB(x, image.getHeight()-y-1));
	            image.setRGB(x, image.getHeight()-y-1, tmp);
	        }
	    }
	}
	/**
	 * Flip the image around the X axis
	 */
	public void flipX()
	{
	    for (int y=0; y < image.getHeight(); y++)
	    {
	        for (int x=0; x < image.getWidth() / 2; x++)
	        {
	            int tmp = image.getRGB(x, y);
	            image.setRGB(x, y, image.getRGB(image.getWidth()-x-1, y));
	            image.setRGB(image.getWidth()-x-1, y, tmp);
	        }
	    }
	}
	/**
	 * Adds the given filter to the Image's list of filters, and runs the filter.
	 * @param filter Can be any class the implements the ImageFilter interface
	 */
	public void addFilter(ImageFilter filter)
	{
		filters.add(filter);
		filter.filter(this);
	}
	/**
	 * Returns the image data of this Image
	 * @return BufferedImage that contains the pixel data
	 */
	public BufferedImage getImage()
	{
		return this.image;
	}
	/** 
	 * Gets the Shape that this Image used to create its BufferedImage.
	 * @return Shape of the current image, not necessarily the original shape if the image has been rotated 
	 */
	public Shape getShape() {
		return shape;
	}
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	/**
	 * Render the Image using the internal coordinates of the Image. 
	 * Can also render elsewhere using getImage() if desired.
	 * @param g2d Graphics object to render with
	 * @param debug Can be used to test stuff, doesn't do anything at the moment
	 */
	public void render(Graphics2D g2d, boolean debug)
	{
		g2d.drawImage(this.image, (int)x, (int)y, this.width, this.height, null);
	}
	/** 
	 * Get the X coordinate of the center of this image
	 * @return x position of image + (width of image / 2)
	 */
	public double getCenterX()
	{
		return x + (this.width / 2);
	}
	/**
	 * Get the Y coordinate of the center of this image
	 * @return y position of image + (height of image / 2)
	 */
	public double getCenterY()
	{
		return y + (this.height / 2);
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x - (this.width / 2);
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y - (this.height / 2);
	}
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	public void setImage(BufferedImage image)
	{
		this.image = image;
	}

	public static int getImageCount() {
		return imageCount;
	}
}
