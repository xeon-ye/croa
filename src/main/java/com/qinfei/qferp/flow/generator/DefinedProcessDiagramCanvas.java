package com.qinfei.qferp.flow.generator;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.imageio.ImageIO;

import org.flowable.bpmn.model.AssociationDirection;
import org.flowable.bpmn.model.GraphicInfo;
import org.flowable.image.impl.DefaultProcessDiagramCanvas;
import org.flowable.image.util.ReflectUtil;

/**
 * 自定义画图工具；
 * 
 * @Author ：Yuan；
 * @Date ：2019/2/22 0022 17:11；
 */
class DefinedProcessDiagramCanvas extends DefaultProcessDiagramCanvas {
	// 调用父类的构造函数集合；
	public DefinedProcessDiagramCanvas(int width, int height, int minX, int minY, String imageType, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) {
		super(width, height, minX, minY, imageType, activityFontName, labelFontName, annotationFontName, customClassLoader);
	}

	/**
	 * 初始化；
	 * 
	 * @param imageType：图片类型；
	 */
	@Override
	public void initialize(String imageType) {
		if ("png".equalsIgnoreCase(imageType)) {
			this.processDiagram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
		} else {
			this.processDiagram = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
		}

		this.g = processDiagram.createGraphics();
		if (!"png".equalsIgnoreCase(imageType)) {
			this.g.setBackground(new Color(255, 255, 255, 0));
			this.g.clearRect(0, 0, canvasWidth, canvasHeight);
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setPaint(Color.black);

		Font font = new Font(activityFontName, Font.BOLD, 13);
		g.setFont(font);
		this.fontMetrics = g.getFontMetrics();

		LABEL_FONT = new Font(labelFontName, Font.BOLD, 13);
		ANNOTATION_FONT = new Font(annotationFontName, Font.PLAIN, 13);

		try {
			USERTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/userTask.png", customClassLoader));
			SCRIPTTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/scriptTask.png", customClassLoader));
			SERVICETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/serviceTask.png", customClassLoader));
			RECEIVETASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/receiveTask.png", customClassLoader));
			SENDTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/sendTask.png", customClassLoader));
			MANUALTASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/manualTask.png", customClassLoader));
			BUSINESS_RULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/businessRuleTask.png", customClassLoader));
			SHELL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/shellTask.png", customClassLoader));
			DMN_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/dmnTask.png", customClassLoader));
			CAMEL_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/camelTask.png", customClassLoader));
			MULE_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/muleTask.png", customClassLoader));
			HTTP_TASK_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/httpTask.png", customClassLoader));

			TIMER_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/timer.png", customClassLoader));
			COMPENSATE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/compensate-throw.png", customClassLoader));
			COMPENSATE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/compensate.png", customClassLoader));
			ERROR_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/error-throw.png", customClassLoader));
			ERROR_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/error.png", customClassLoader));
			MESSAGE_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/message-throw.png", customClassLoader));
			MESSAGE_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/message.png", customClassLoader));
			SIGNAL_THROW_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/signal-throw.png", customClassLoader));
			SIGNAL_CATCH_IMAGE = ImageIO.read(ReflectUtil.getResource("org/flowable/icons/signal.png", customClassLoader));
		} catch (IOException e) {
			LOGGER.warn("Could not load image for process diagram creation: {}", e.getMessage());
		}
	}

	public void drawConnection(int[] xPoints, int[] yPoints, boolean conditional, boolean isDefault, String connectionType, AssociationDirection associationDirection, boolean highLighted, double scaleFactor) {

		Paint originalPaint = g.getPaint();
		Stroke originalStroke = g.getStroke();

		g.setPaint(CONNECTION_COLOR);
		if (connectionType.equals("association")) {
			g.setStroke(ASSOCIATION_STROKE);
		} else if (highLighted) {
			g.setPaint(HIGHLIGHT_COLOR);
			g.setStroke(new BasicStroke(2.6f));
		}

		for (int i = 1; i < xPoints.length; i++) {
			Integer sourceX = xPoints[i - 1];
			Integer sourceY = yPoints[i - 1];
			Integer targetX = xPoints[i];
			Integer targetY = yPoints[i];
			Line2D.Double line = new Line2D.Double(sourceX, sourceY, targetX, targetY);
			g.draw(line);
		}

		if (isDefault) {
			Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
			drawDefaultSequenceFlowIndicator(line, scaleFactor);
		}

		if (conditional) {
			Line2D.Double line = new Line2D.Double(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
			drawConditionalSequenceFlowIndicator(line, scaleFactor);
		}

		if (associationDirection == AssociationDirection.ONE || associationDirection == AssociationDirection.BOTH) {
			Line2D.Double line = new Line2D.Double(xPoints[xPoints.length - 2], yPoints[xPoints.length - 2], xPoints[xPoints.length - 1], yPoints[xPoints.length - 1]);
			drawArrowHead(line, scaleFactor);
		}
		if (associationDirection == AssociationDirection.BOTH) {
			Line2D.Double line = new Line2D.Double(xPoints[1], yPoints[1], xPoints[0], yPoints[0]);
			drawArrowHead(line, scaleFactor);
		}
		g.setPaint(originalPaint);
		g.setStroke(originalStroke);
	}

	public void drawLabel(String text, GraphicInfo graphicInfo, boolean centered) {
		float interline = 1.0f;

		// text
		if (text != null && text.length() > 0) {
			Paint originalPaint = g.getPaint();
			Font originalFont = g.getFont();

			g.setPaint(new Color(0, 0, 0));
			g.setFont(LABEL_FONT);

			int wrapWidth = 100;
			int textY = (int) graphicInfo.getY();

			// TODO: use drawMultilineText()
			AttributedString as = new AttributedString(text);
			as.addAttribute(TextAttribute.FOREGROUND, g.getPaint());
			as.addAttribute(TextAttribute.FONT, g.getFont());
			AttributedCharacterIterator aci = as.getIterator();
			FontRenderContext frc = new FontRenderContext(null, true, false);
			LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);

			while (lbm.getPosition() < text.length()) {
				TextLayout tl = lbm.nextLayout(wrapWidth);
				textY += tl.getAscent();
				Rectangle2D bb = tl.getBounds();
				double tX = graphicInfo.getX();
				tX += (int) (graphicInfo.getWidth() / 2 - bb.getWidth() / 2);
				tl.draw(g, (float) tX, textY);
				textY += tl.getDescent() + tl.getLeading() + (interline - 1.0f) * tl.getAscent();
			}

			// restore originals
			g.setFont(originalFont);
			g.setPaint(originalPaint);
		}
	}
}