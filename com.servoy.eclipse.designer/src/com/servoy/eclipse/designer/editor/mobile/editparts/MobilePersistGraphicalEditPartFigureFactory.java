/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2012 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */

package com.servoy.eclipse.designer.editor.mobile.editparts;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import com.servoy.base.persistence.IMobileProperties;
import com.servoy.eclipse.designer.Activator;
import com.servoy.eclipse.designer.editor.IFigureFactory;
import com.servoy.eclipse.designer.editor.PersistImageFigure;
import com.servoy.eclipse.designer.editor.SetBoundsToSupportBoundsFigureListener;
import com.servoy.eclipse.designer.internal.core.IImageNotifier;
import com.servoy.eclipse.designer.internal.core.ImageDataCollector;
import com.servoy.eclipse.designer.internal.core.ImageNotifierSupport;
import com.servoy.eclipse.designer.internal.core.PersistImageNotifier;
import com.servoy.eclipse.ui.resource.FontResource;
import com.servoy.j2db.IApplication;
import com.servoy.j2db.component.ComponentFactory;
import com.servoy.j2db.dataprocessing.CustomValueList;
import com.servoy.j2db.dataprocessing.IValueList;
import com.servoy.j2db.persistence.AbstractBase;
import com.servoy.j2db.persistence.Field;
import com.servoy.j2db.persistence.Form;
import com.servoy.j2db.persistence.GraphicalComponent;
import com.servoy.j2db.persistence.IColumnTypes;
import com.servoy.j2db.persistence.IPersist;
import com.servoy.j2db.persistence.ISupportBounds;
import com.servoy.j2db.smart.dataui.DataCheckBox;
import com.servoy.j2db.smart.dataui.DataChoice;
import com.servoy.j2db.smart.dataui.DataComboBox;
import com.servoy.j2db.smart.dataui.DataField;
import com.servoy.j2db.smart.dataui.ScriptButton;
import com.servoy.j2db.util.gui.RoundedBorder;

/**
 * Factory for creating figures for persists in mobile form editor.
 * 
 * @author rgansevles
 *
 */
public class MobilePersistGraphicalEditPartFigureFactory implements IFigureFactory<PersistImageFigure>
{
	private final IApplication application;
	private final Form form;

	public MobilePersistGraphicalEditPartFigureFactory(IApplication application, Form form)
	{
		this.application = application;
		this.form = form;
	}

	public PersistImageFigure createFigure(final GraphicalEditPart editPart)
	{
		final IPersist persist = (IPersist)editPart.getModel();
		PersistImageFigure figure = new PersistImageFigure(application, persist, form)
		{
			@Override
			protected IImageNotifier createImageNotifier()
			{
				return new PersistImageNotifier(application, persist, form, this)
				{
					@Override
					protected Component createComponent()
					{
						if (persist instanceof AbstractBase)
						{
							String styleLookupname = null;
							if (persist instanceof GraphicalComponent &&
								((AbstractBase)persist).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName) != null)
							{
								styleLookupname = "headertext";
							}
							((AbstractBase)persist).setRuntimeProperty(ComponentFactory.STYLE_LOOKUP_NAME, styleLookupname);
						}
						Component component = super.createComponent();
						component.setFont(component.getFont().deriveFont(12f)); // 12 pt font

						if (component instanceof JButton || component instanceof DataComboBox || component instanceof DataField ||
							component instanceof DataChoice || component instanceof DataCheckBox)
						{
							Color roundedBorderColor = new Color(20, 80, 114);
							RoundedBorder rborder = new RoundedBorder(1, 1, 1, 1, roundedBorderColor, roundedBorderColor, roundedBorderColor,
								roundedBorderColor);
							rborder.setRoundingRadius(new float[] { 10, 10, 10, 10 });
							((JComponent)component).setBorder(rborder);
						}


						if (component instanceof JButton)
						{
							((JButton)component).setUI(new BasicButtonUI());
							if (persist instanceof AbstractBase)
							{
								String dataIcon = (String)((AbstractBase)persist).getCustomMobileProperty(IMobileProperties.DATA_ICON.propertyName);
								if (dataIcon != null)
								{
									ImageIcon icon = Activator.getDefault().loadImageIconFromBundle("mobile/icons-18-white-" + dataIcon + ".png");
									if (icon != null)
									{
										if (component instanceof ScriptButton)
										{
											((ScriptButton)component).setMediaOption(1);
										}
//										((JButton)component).setHorizontalTextPosition(SwingConstants.CENTER); // TODO: how to center the text and keep icon left?
										((JButton)component).setHorizontalAlignment(SwingConstants.LEFT);
										((JButton)component).setIcon(new IconWithRoundBackground(icon));
									}
								}
							}
						}
						else if (component instanceof JLabel)
						{
							((JLabel)component).setOpaque(false);
							if (!(persist instanceof AbstractBase && ((AbstractBase)persist).getCustomMobileProperty(IMobileProperties.HEADER_TEXT.propertyName) != null))
							{
								Object headerSizeProp = ((AbstractBase)persist).getCustomMobileProperty(IMobileProperties.HEADER_SIZE.propertyName);
								int headerSize = headerSizeProp instanceof Integer ? Math.max(1, Math.min(6, ((Integer)headerSizeProp).intValue())) : 4;

								float fontsize;
								switch (headerSize)
								{
									case 1 :
										fontsize = 24f;
										break;
									case 2 :
										fontsize = 18f;
										break;
									case 3 :
										fontsize = 14f;
										break;
									case 5 :
										fontsize = 10f;
										break;
									case 6 :
										fontsize = 8f;
										break;
									default : // 4
										fontsize = 12f;
										break;
								}
								component.setFont(component.getFont().deriveFont(fontsize));
							}
						}
						else if (component instanceof DataComboBox)
						{
							((DataComboBox)component).setEditable(false);
							((DataComboBox)component).setUI(new BasicComboBoxUI()
							{
								@Override
								protected JButton createArrowButton()
								{
									JButton button = new JButton(new IconWithRoundBackground(Activator.getDefault().loadImageIconFromBundle(
										"mobile/icons-18-white-arrow-d.png")));
									button.setBorder(null);
									button.setOpaque(false);
									return button;
								}
							});
						}
						else if (component instanceof DataChoice &&
							(((DataChoice)component).getChoiceType() == Field.RADIOS || ((DataChoice)component).getChoiceType() == Field.CHECKS))
						{
							DataChoice dataChoice = (DataChoice)component;
							boolean horizontal = IMobileProperties.RADIO_STYLE_HORIZONTAL.equals(((AbstractBase)persist).getCustomMobileProperty(IMobileProperties.RADIO_STYLE.propertyName));

							component.setFont(component.getFont().deriveFont(Font.BOLD));
							if (dataChoice.getChoiceType() == Field.RADIOS)
							{
								((JRadioButton)dataChoice.getRendererComponent()).setIcon(Activator.getDefault().loadImageIconFromBundle(
									horizontal ? "empty_18x18.png" : "mobile/radio_off.png"));
								((JRadioButton)dataChoice.getRendererComponent()).setSelectedIcon(Activator.getDefault().loadImageIconFromBundle(
									horizontal ? "empty_18x18.png" : "mobile/radio_on.png"));
							}
							else
							{
								((JCheckBox)dataChoice.getRendererComponent()).setIcon(Activator.getDefault().loadImageIconFromBundle("mobile/check_off.png"));
								((JCheckBox)dataChoice.getRendererComponent()).setSelectedIcon(Activator.getDefault().loadImageIconFromBundle(
									"mobile/check_on.png"));
							}

							IValueList valueList = dataChoice.getValueList();
							if (valueList.getSize() == 0)
							{
								// some sample values
								dataChoice.setValueList(valueList = new CustomValueList(application, null, "One\nTwo\nThree", false, IColumnTypes.TEXT, null));
							}
							// select first item
							dataChoice.setValueObject(valueList.getRealElementAt(0));
							dataChoice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
							if (!horizontal)
							{
								dataChoice.getEnclosedComponent().setVisibleRowCount(valueList.getSize());
								dataChoice.getEnclosedComponent().setLayoutOrientation(JList.VERTICAL);
							}
							Dimension compPrefsize = component.getPreferredSize();
							setPreferredSize(compPrefsize.width, compPrefsize.height);
						}
						else if (component instanceof DataCheckBox)
						{
							((DataCheckBox)component).setIcon(Activator.getDefault().loadImageIconFromBundle("mobile/check_on.png"));
						}
						return component;
					}

					@Override
					protected ImageDataCollector createImageDataCollector(ImageNotifierSupport imageNotifierSupport)
					{
						return new ImageDataCollector(imageNotifierSupport)
						{
							@Override
							protected void setGraphicsClip(Graphics graphics, Component component, int width, int height)
							{
								if (graphics instanceof Graphics2D && component instanceof JLabel && ((JLabel)component).getComponentCount() == 1)
								{
									Component comp = ((JLabel)component).getComponent(0);
									if (comp instanceof JComponent)
									{
										Border compBorder = ((JComponent)comp).getBorder();
										if (compBorder instanceof RoundedBorder)
										{
											// fake soft clipping
											Shape clip = ((RoundedBorder)compBorder).createRoundedShape(width, height);
											((Graphics2D)graphics).setComposite(AlphaComposite.Src);
											((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
											graphics.setColor(Color.WHITE);
											((Graphics2D)graphics).fill(clip);
											((Graphics2D)graphics).setComposite(AlphaComposite.SrcAtop);
											return;
										}
									}
								}

								super.setGraphicsClip(graphics, component, width, height);
							}
						};
					}
				};
			}

			@Override
			protected org.eclipse.swt.graphics.Font createDrawnameFont()
			{
				return FontResource.getDefaultFont(SWT.NORMAL, 1);
			}

			@Override
			public void setPreferredSize(org.eclipse.draw2d.geometry.Dimension size)
			{
				if (prefSize == null || !prefSize.equals(size))
				{
					prefSize = size;
					Display.getDefault().asyncExec(new Runnable()
					{
						public void run()
						{
							revalidate();
						}
					});
				}
			}

			@Override
			public org.eclipse.draw2d.geometry.Dimension getPreferredSize(int wHint, int hHint)
			{
				if (persist instanceof Field && ((Field)persist).getDisplayType() != Field.CHECKS && ((Field)persist).getDisplayType() != Field.RADIOS)
				{
					// always calculate, see setPreferredSize call for checks/radios
					return super.getPreferredSize(wHint, hHint);
				}
				return prefSize != null ? prefSize : super.getPreferredSize(wHint, hHint);
			}
		};

		figure.addFigureListener(new SetBoundsToSupportBoundsFigureListener(form, (ISupportBounds)editPart.getModel(), false));
		return figure;
	}
}
