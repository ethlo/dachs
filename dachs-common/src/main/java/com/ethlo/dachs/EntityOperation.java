package com.ethlo.dachs;

public enum EntityOperation
{
	CREATE('c'), READ('r'), UPDATE('u'), DELETE('d'), DISABLE('x'), ENABLE('e');
	
	private char id;

	private EntityOperation(char id)
	{
		this.id = id;
	}

	public char getId() 
	{
		return this.id;
	}

	public EntityOperation getEnum() 
	{
		return this;
	}

	public static EntityOperation valueOf(char operation) 
	{
		for (EntityOperation op : EntityOperation.values())
		{
			if (op.getId() == operation)
			{
				return op;
			}
		}
		throw new IllegalArgumentException("No CRUD operation with id " + operation);
	}
}