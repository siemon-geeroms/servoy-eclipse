/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2014 Servoy BV

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

package com.servoy.eclipse.designer.rfb.startup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.sablo.specification.PropertyDescription;
import org.sablo.specification.WebComponentPackageSpecification;
import org.sablo.specification.WebComponentSpecProvider;
import org.sablo.specification.WebComponentSpecification;
import org.sablo.specification.WebLayoutSpecification;

import com.servoy.j2db.server.ngclient.FormElement;
import com.servoy.j2db.server.ngclient.template.FormTemplateGenerator;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.HTTPUtils;

/**
 * Filter for designer editor
 * @author gboros
 */
@WebFilter(urlPatterns = { "/designer/*" })
@SuppressWarnings("nls")
public class DesignerFilter implements Filter
{
	private static final List<String> IGNORE_LIST = Arrays.asList(new String[] { "servoydefault-checkgroup", FormElement.ERROR_BEAN, "servoydefault-navigator", "servoydefault-radiogroup", "servoydefault-htmlview", "colorthefoundset" });

	public static final String DROPPABLE = "droppable";

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
	{
		try
		{
			HttpServletRequest request = (HttpServletRequest)servletRequest;
			String uri = request.getRequestURI();
			if (uri != null && uri.endsWith("palette"))
			{
				WebComponentSpecProvider provider = WebComponentSpecProvider.getInstance();

				((HttpServletResponse)servletResponse).setContentType("application/json");

				if (servletResponse instanceof HttpServletResponse) HTTPUtils.setNoCacheHeaders((HttpServletResponse)servletResponse);

				try
				{
					JSONWriter jsonWriter = new JSONWriter(servletResponse.getWriter());
					jsonWriter.array();
					// first add all the layout containers.
					for (WebComponentPackageSpecification<WebLayoutSpecification> entry : provider.getLayoutSpecifications().values())
					{
						jsonWriter.object();
						jsonWriter.key("packageName").value(entry.getPackageName());
						jsonWriter.key("packageDisplayname").value(entry.getPackageDisplayname());
						jsonWriter.key("components");
						jsonWriter.array();

						for (WebLayoutSpecification spec : entry.getSpecifications().values())
						{
							jsonWriter.object();
							jsonWriter.key("name").value(spec.getName());
							if (spec.getConfig() != null)
							{
								String layoutName = new JSONObject((String)spec.getConfig()).optString("layoutName", null);
								if (layoutName != null)
								{
									jsonWriter.key("layoutName").value(layoutName);
								}
								else jsonWriter.key("layoutName").value(spec.getName());
							}
							else jsonWriter.key("layoutName").value(spec.getName());
							jsonWriter.key("componentType").value("layout");
							jsonWriter.key("displayName").value(spec.getDisplayName());
							JSONObject config = spec.getConfig() instanceof String ? new JSONObject((String)spec.getConfig()) : null;
							if (config == null)
							{
								jsonWriter.key("tagName").value("<div style='border-style: dotted;'></div>"); //TODO is tagname configurable by the spec
							}
							else
							{
								jsonWriter.key("tagName").value(createLayoutDiv(config, new StringBuilder()).toString());
							}
							Map<String, Object> model = new HashMap<String, Object>();
							PropertyDescription pd = spec.getProperty("size");
							if (pd != null && pd.getDefaultValue() != null)
							{
								model.put("size", pd.getDefaultValue());
							}
							else
							{
								HashMap<String, Number> size = new HashMap<String, Number>();
								size.put("height", Integer.valueOf(20));
								size.put("width", Integer.valueOf(100));
								model.put("size", size);
							}
							jsonWriter.key("model").value(new JSONObject(model));
							if (spec.getIcon() != null)
							{
								jsonWriter.key("icon").value(spec.getIcon());
							}
							jsonWriter.key("topContainer").value(spec.isTopContainer());


							jsonWriter.endObject();
						}

						jsonWriter.endArray();
						jsonWriter.endObject();
					}

					List<WebComponentPackageSpecification<WebComponentSpecification>> packages = new ArrayList<>(
						provider.getWebComponentSpecifications().values());
					Collections.sort(packages, new Comparator<WebComponentPackageSpecification<WebComponentSpecification>>()
					{
						@Override
						public int compare(WebComponentPackageSpecification<WebComponentSpecification> pkg1,
							WebComponentPackageSpecification<WebComponentSpecification> pkg2)
						{
							if (pkg1.getPackageName().equals("servoydefault")) return -1;
							if (pkg2.getPackageName().equals("servoydefault")) return 1;
							return pkg1.getPackageDisplayname().compareTo(pkg1.getPackageDisplayname());
						}
					});

					for (WebComponentPackageSpecification<WebComponentSpecification> pkg : packages)
					{
						jsonWriter.object();
						jsonWriter.key("packageName").value(pkg.getPackageName());
						jsonWriter.key("packageDisplayname").value(pkg.getPackageDisplayname());
						jsonWriter.key("components");
						jsonWriter.array();
						for (WebComponentSpecification spec : pkg.getSpecifications().values())
						{
							if (!IGNORE_LIST.contains(spec.getName()))
							{
								jsonWriter.object();
								jsonWriter.key("name").value(spec.getName());
								jsonWriter.key("componentType").value("component");
								jsonWriter.key("displayName").value(spec.getDisplayName());
								jsonWriter.key("tagName").value(FormTemplateGenerator.getTagName(spec.getName()));
								Map<String, Object> model = new HashMap<String, Object>();
								PropertyDescription pd = spec.getProperty("size");
								if (pd != null && pd.getDefaultValue() != null)
								{
									model.put("size", pd.getDefaultValue());
								}
								if (spec.getProperty("enabled") != null)
								{
									model.put("enabled", Boolean.TRUE);
								}
								if (spec.getProperty("editable") != null)
								{
									model.put("editable", Boolean.TRUE);
								}
								if ("servoydefault-label".equals(spec.getName()))
								{
									model.put("text", "label");
								}
								jsonWriter.key("model").value(new JSONObject(model));
								if (spec.getIcon() != null)
								{
									jsonWriter.key("icon").value(spec.getIcon());
								}
								jsonWriter.key("types").value(new JSONArray(getPalleteTypeNames(spec)));
								jsonWriter.endObject();
							}
						}
						jsonWriter.endArray();
						jsonWriter.endObject();
					}
					jsonWriter.endArray();
				}
				catch (JSONException ex)
				{
					Debug.error("Exception during designe palette generation", ex);
				}

				return;
			}

			filterChain.doFilter(request, servletResponse);
		}
		catch (RuntimeException e)
		{
			Debug.error(e);
			throw e;
		}
	}

	/**
	 * @param config
	 * @param sb
	 * @param specifications
	 * @throws JSONException
	 */
	protected StringBuilder createLayoutDiv(JSONObject config, StringBuilder sb) throws JSONException
	{
		sb.append("<div style='border-style: dotted;' "); // TODO tagname from spec?
		Iterator keys = config.keys();
		while (keys.hasNext())
		{
			String key = (String)keys.next();
			if (key.equals("children") || key.equals("layoutName")) continue;
			String value = config.getString(key);
			sb.append(key);
			sb.append("='");
			sb.append(value);
			sb.append("' ");
		}
		sb.append(">");
		JSONArray children = config.optJSONArray("children");
		if (children != null)
		{
			for (int i = 0; i < children.length(); i++)
			{
				JSONObject jsonObject = children.getJSONObject(i);
				JSONObject childModel = jsonObject.optJSONObject("model");
				if (childModel != null)
				{
					createLayoutDiv(childModel, sb);
				}
			}
		}
		return sb.append("</div>");
	}

	private List<String> getPalleteTypeNames(WebComponentSpecification spec)
	{
		List<String> result = new ArrayList<String>();
		Map<String, PropertyDescription> properties = spec.getProperties();
		for (PropertyDescription propertyDescription : properties.values())
		{
			Object configObject = propertyDescription.getConfig();
			if (configObject instanceof JSONObject && Boolean.TRUE.equals(((JSONObject)configObject).opt(DROPPABLE)))
			{
				String simpleTypeName = propertyDescription.getType().getName().replaceFirst(spec.getName() + ".", "");
				if (spec.getFoundTypes().containsKey(simpleTypeName)) result.add(simpleTypeName);
			}
		}
		return result;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}
}