package com.ethlo.dachs;

/**
 *
 */
public enum CrudOperation
{
	CREATE('c'), READ('r'), UPDATE('u'), DELETE('d'), DISABLE('x');
	
	private final char id;

	CrudOperation(char id)
	{
		this.id = id;
	}

	public char getId() 
	{
		return this.id;
	}

	public CrudOperation getEnum() 
	{
		return this;
	}

	public static CrudOperation valueOf(char operation) 
	{
		for (CrudOperation op : CrudOperation.values())
		{
			if (op.getId() == operation)
			{
				return op;
			}
		}
		throw new IllegalArgumentException("No CRUD operation with id " + operation);
	}
}