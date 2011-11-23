package com.servoy.eclipse.debug.script;

import org.eclipse.dltk.javascript.typeinfo.IRType;
import org.eclipse.dltk.javascript.typeinfo.IRTypeFactory;
import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
import org.eclipse.dltk.javascript.typeinfo.model.Type;
import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;

public class JavaClassRuntimeTypeFactory implements IRTypeFactory
{
	public IRType create(Type type)
	{
		if (type.getAttribute(TypeProvider.JAVA_CLASS) != null)
		{
			return new JavaRuntimeType(type);
		}
		return null;
	}

	private static class JavaRuntimeType implements IRType, SimpleType
	{

		private final Type type;

		/**
		 * @param type
		 */
		public JavaRuntimeType(Type type)
		{
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.model.JSType#getKind()
		 */
		public TypeKind getKind()
		{
			return type.getKind();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.model.JSType#getName()
		 */
		public String getName()
		{
			return type.getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.JSType2#isArray()
		 */
		public boolean isArray()
		{
			return ((Class< ? >)type.getAttribute(TypeProvider.JAVA_CLASS)).isArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.JSType2#isAssignableFrom(org.eclipse.dltk.javascript.typeinfo.JSType2)
		 */
		public boolean isAssignableFrom(IRType runtimeType)
		{
			if (runtimeType instanceof JavaRuntimeType)
			{
				Class< ? > cls = (Class< ? >)type.getAttribute(TypeProvider.JAVA_CLASS);
				Class< ? > other = (Class< ? >)((JavaRuntimeType)runtimeType).type.getAttribute(TypeProvider.JAVA_CLASS);
				return cls.isAssignableFrom(other);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.model.ClassType#getTarget()
		 */
		public Type getTarget()
		{
			return type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.model.ClassType#setTarget(org.eclipse.dltk.javascript.typeinfo.model.Type)
		 */
		public void setTarget(Type value)
		{
			//ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (obj.getClass() == JavaRuntimeType.class)
			{
				return ((JavaRuntimeType)obj).type.equals(type);
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return type.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.dltk.javascript.typeinfo.IRType#activeTypeSystem(org.eclipse.dltk.javascript.typeinfo.ITypeSystem)
		 */
		public ITypeSystem activeTypeSystem(ITypeSystem fallback)
		{
			return fallback;
		}

	}
}
