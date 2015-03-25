package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;

import java.util.Stack;
import java.util.EmptyStackException;
import java.lang.ArithmeticException;

import java.util.HashMap;

public class ConstantFolder
{
	
	private enum valueType {
		INT, FLOAT, LONG, DOUBLE, OTHER
	}
	
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private void simpleFold (ConstantPoolGen cpgen) {
		
	}
	
	private static void storeVariable (InstructionHandle pHandle, InstructionHandle handle, ConstantPoolGen cpgen, HashMap<Integer,Number> variablesList) {
		Instruction instruction = handle.getInstruction();
		Instruction pInstruction = pHandle.getInstruction();
		
		if (instruction instanceof ISTORE) {
			ISTORE ISTOREInstruction = (ISTORE)(instruction);
			int index = ISTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle,variablesList);
			variablesList.put(index, value.intValue());
		}
		else if (instruction instanceof FSTORE) {
			FSTORE FSTOREInstruction = (FSTORE)(instruction);
			int index = FSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle,variablesList);
			variablesList.put(index, value.floatValue());
		}
		else if (instruction instanceof LSTORE) {
			LSTORE LSTOREInstruction = (LSTORE)(instruction);
			int index = LSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle,variablesList);
			variablesList.put(index, value.longValue());
		}
		else if (instruction instanceof DSTORE) {
			DSTORE DSTOREInstruction = (DSTORE)(instruction);
			int index = DSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle,variablesList);
			variablesList.put(index, value.doubleValue());
		}
	}
	
	private static valueType getInstructionReturnType (InstructionHandle handle) {
		Instruction instruction = handle.getInstruction();
		
		if (instruction instanceof IADD || instruction instanceof ISUB
				|| instruction instanceof IMUL || instruction instanceof IDIV
				|| instruction instanceof IREM) {
				
				return valueType.INT;
			}
			else if (instruction instanceof FADD || instruction instanceof FSUB
					|| instruction instanceof FMUL || instruction instanceof FDIV
					|| instruction instanceof FREM) {
				return valueType.FLOAT;
			}
			else if (instruction instanceof LADD || instruction instanceof LSUB
					|| instruction instanceof LMUL || instruction instanceof LDIV
					|| instruction instanceof LREM) {
				
				
				return valueType.LONG;
			}
			else if (instruction instanceof DADD || instruction instanceof DSUB
					|| instruction instanceof DMUL || instruction instanceof DDIV
					|| instruction instanceof DREM) {
				
				return valueType.DOUBLE;
			}
			else {
				return valueType.OTHER;
			}
	}
	
	private static Number getVal (ConstantPoolGen cpgen, InstructionHandle handle, HashMap<Integer,Number>  variablesList) {
		
		Instruction instruction = handle.getInstruction();
		Number value = 0;
		
		if (instruction instanceof LDC) {
			LDC LDCInstruction = (LDC)(instruction);
			Object LDCValue = LDCInstruction.getValue(cpgen);
			if (LDCValue instanceof Integer) {
				value = (int)(LDCValue);
			}
			else if (LDCValue instanceof Float) {
				value = (float)(LDCValue);
			}
		}
		else if (instruction instanceof LDC_W) {
			LDC_W LDC_WInstruction = (LDC_W)(instruction);
			Object LDC_WValue = LDC_WInstruction.getValue(cpgen);
			if (LDC_WValue instanceof Integer) {
				value = (int)(LDC_WValue);
			}
			else if (LDC_WValue instanceof Float) {
				value = (float)(LDC_WValue);
			}
		}
		else if (instruction instanceof LDC2_W) {
			LDC2_W LDC2_WInstruction = (LDC2_W)(instruction);
			Object LDC2_WValue = LDC2_WInstruction.getValue(cpgen);
			if (LDC2_WValue instanceof Long) {
				value = (long)(LDC2_WValue);
			}
			else if (LDC2_WValue instanceof Double) {
				value = (double)(LDC2_WValue);
			}
		}
		else if (instruction instanceof ICONST) {
			ICONST ICONSTInstruction = (ICONST)(instruction);
			Number ICONSTValue = ICONSTInstruction.getValue();
			value = ICONSTValue.intValue();
		}
		else if (instruction instanceof FCONST) {
			FCONST FCONSTInstruction = (FCONST)(instruction);
			Number FCONSTValue = FCONSTInstruction.getValue();
			value = FCONSTValue.floatValue();
		}
		else if (instruction instanceof LCONST) {
			LCONST LCONSTInstruction = (LCONST)(instruction);
			Number LCONSTValue = LCONSTInstruction.getValue();
			value = LCONSTValue.longValue();
		}
		else if (instruction instanceof DCONST) {
			DCONST DCONSTInstruction = (DCONST)(instruction);
			Number DCONSTValue = DCONSTInstruction.getValue();
			value = DCONSTValue.doubleValue();
		}
		else if (instruction instanceof BIPUSH) {
			BIPUSH BIPUSHInstruction = (BIPUSH)(instruction);
			Number BIPUSHValue = BIPUSHInstruction.getValue();
			value = BIPUSHValue.intValue();				
		}
		else if (instruction instanceof SIPUSH) {
			SIPUSH SIPUSHInstruction = (SIPUSH)(instruction);
			Number SIPUSHValue = SIPUSHInstruction.getValue();
			value = SIPUSHValue.intValue();
		}
		else {
			try {
				
				if (instruction instanceof ILOAD) {
					ILOAD ILOADInstruction = (ILOAD)(instruction);
					int index = ILOADInstruction.getIndex();
					Number ILOADValue = variablesList.get(index);
					value = ILOADValue.intValue();
				}
				else if (instruction instanceof FLOAD) {
					FLOAD FLOADInstruction = (FLOAD)(instruction);
					int index = FLOADInstruction.getIndex();
					Number FLOADValue = variablesList.get(index);
					value = FLOADValue.floatValue();
				}
				else if (instruction instanceof LLOAD) {
					LLOAD LLOADInstruction = (LLOAD)(instruction);
					int index = LLOADInstruction.getIndex();
					Number LLOADValue = variablesList.get(index);
					value = LLOADValue.longValue();
				}
				else if (instruction instanceof DLOAD) {
					DLOAD DLOADInstruction = (DLOAD)(instruction);
					int index = DLOADInstruction.getIndex();
					Number DLOADValue = variablesList.get(index);
					value = DLOADValue.doubleValue();
				}
			}
			catch (NullPointerException e) {
				
			}
		}

		return value;
	}
	
	private static Number getResult (Number ppVal, Number pVal, InstructionHandle handle) {
		
		Instruction instruction = handle.getInstruction();
		valueType type = getInstructionReturnType(handle);
		Number result = 0;
		
		try {
		
			switch (type) {
				
				case INT:
					
					int inta = ppVal.intValue();
					int intb = pVal.intValue();
					
					if (instruction instanceof IADD){
						result = inta + intb;
					}
					else if (instruction instanceof ISUB){
						result = inta - intb;
					}
					else if (instruction instanceof IMUL){
						result = inta * intb;
					}
					else if (instruction instanceof IDIV){
						result = inta / intb;
					}
					else if (instruction instanceof IREM){
						result = inta % intb;
					}
					
					break;
					
				case FLOAT:
					
					float floata = ppVal.floatValue();
					float floatb = pVal.floatValue();
					
					if (instruction instanceof FADD){
						result = floata + floatb;
					}
					else if (instruction instanceof FSUB){
						result = floata - floatb;
					}
					else if (instruction instanceof FMUL){
						result = floata * floatb;
					}
					else if (instruction instanceof FDIV){
						result = floata / floatb;
					}
					else if (instruction instanceof FREM){
						result = floata % floatb;
					}
					
					break;
					
				case LONG:
						
					long longa = ppVal.longValue();
					long longb = pVal.longValue();
					
					if (instruction instanceof LADD){
						result = longa + longb;
					}
					else if (instruction instanceof LSUB){
						result = longa - longb;
					}
					else if (instruction instanceof LMUL){
						result = longa * longb;
					}
					else if (instruction instanceof LDIV){
						result = longa / longb;
					}
					else if (instruction instanceof LREM){
						result = longa % longb;
					}
					
					break;
					
				case DOUBLE:
					
					double doublea = ppVal.doubleValue();
					double doubleb = pVal.doubleValue();
					
					if (instruction instanceof DADD){
						result = doublea + doubleb;
					}
					else if (instruction instanceof DSUB){
						result = doublea - doubleb;
					}
					else if (instruction instanceof DMUL){
						result = doublea * doubleb;
					}
					else if (instruction instanceof DDIV){
						result = doublea / doubleb;
					}
					else if (instruction instanceof DREM){
						result = doublea % doubleb;
					}
					
					break;
					
				case OTHER:
					break;
			}
		}
		catch (ArithmeticException e) {
		}
			
		
		return result;
	}
	
	private static InstructionHandle foldInstructions (InstructionHandle ppHandle, InstructionHandle pHandle, InstructionHandle handle,
					InstructionList instList, ConstantPoolGen cpgen, HashMap<Integer,Number> variablesList) {
	
		InstructionHandle newHandle;
		valueType type = getInstructionReturnType(handle);
		
		Number pVal = getVal(cpgen, pHandle, variablesList);
		Number ppVal = getVal(cpgen, ppHandle, variablesList);
		Number result = getResult(ppVal, pVal, handle);
		
		switch (type) {
			case INT:
				/*int val = result.intValue();
				if (val >= -1 && val <= 5) {
					newHandle = instList.append(handle, new ICONST((byte)(val)));
				}
				else if (val >= -128 && val <= 127) {
					newHandle = instList.append(handle, new BIPUSH((byte)(val)));
				}
				else if (val >= -32768 && val <= 32767) {
					newHandle = instList.append(handle, new SIPUSH((short)(val)));
				}
				else {
					newCpIndex = cpgen.addInteger(val);
					newHandle = instList.append(handle, new LDC(newCpIndex));
				}*/
				newHandle = instList.append(handle, new PUSH(cpgen, result.intValue()));
				break;
				
			case FLOAT:
				//newCpIndex = cpgen.addFloat(result.floatValue());;
				//newHandle = instList.append(handle, new LDC_W(newCpIndex));
				newHandle = instList.append(handle, new PUSH(cpgen, result.floatValue()));
				break;
				
			case LONG:
				//newCpIndex = cpgen.addLong(result.longValue());
				//newHandle = instList.append(handle, new LDC2_W(newCpIndex));
				newHandle = instList.append(handle, new PUSH(cpgen, result.longValue()));
				break;
				
			case DOUBLE:
				//newCpIndex = cpgen.addDouble(result.doubleValue());
				//newHandle = instList.append(handle, new LDC2_W(newCpIndex));
				newHandle = instList.append(handle, new PUSH(cpgen, result.doubleValue()));
				break;
				
			default:
				newHandle = handle;
				break;
		}
		
		try {
			instList.delete(pHandle);
			instList.delete(ppHandle);
			instList.delete(handle);
		}
		catch (TargetLostException e)
		{
		     InstructionHandle[] targets = e.getTargets();

		     for(int i=0; i < targets.length; i++) {
		          InstructionTargeter[] targeters = targets[i].getTargeters();

		          for(int j=0; j < targeters.length; j++)
		               targeters[j].updateTarget(targets[i], handle);
		     }
		}
		
		return newHandle;
	}
	
	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		
		Code methodCode = method.getCode();
		InstructionList instList = new InstructionList(methodCode.getCode());
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);
	
		Stack<InstructionHandle> instructionStack = new Stack<InstructionHandle>();

		HashMap<Integer,Number> variablesList = new HashMap<Integer,Number>();
		
		InstructionHandle[] instructionHandles = instList.getInstructionHandles();
		int length = instructionHandles.length;
		int k =0;
		
		for (k = 0; k < length; k++)
		{
			InstructionHandle handle = instructionHandles[k];
			Instruction currentInstruction = handle.getInstruction();
			
			if (currentInstruction instanceof CPInstruction || currentInstruction instanceof ICONST
				|| currentInstruction instanceof FCONST || currentInstruction instanceof LCONST
				|| currentInstruction instanceof DCONST || currentInstruction instanceof BIPUSH
				|| currentInstruction instanceof SIPUSH || (currentInstruction instanceof LoadInstruction && !variablesList.isEmpty())) {
				
				instructionStack.push(handle);

			}
			else if (currentInstruction instanceof StoreInstruction && instructionStack.size() > 0) {
				InstructionHandle pHandle = null;
				try {
					pHandle = (InstructionHandle)(instructionStack.pop());
				}
				catch (EmptyStackException e) {
					e.printStackTrace();
					continue;
				}
				
				storeVariable(pHandle, handle, cpgen, variablesList); // I hope that variablesList is passed by reference here!
				
			}
			else if (currentInstruction instanceof ArithmeticInstruction && instructionStack.size() > 1) {
				InstructionHandle pHandle = null, ppHandle = null;
				try {
					pHandle = (InstructionHandle)(instructionStack.pop());
					ppHandle = (InstructionHandle)(instructionStack.pop());
				}
				catch (EmptyStackException e) {
					e.printStackTrace();
					continue;
				}
				InstructionHandle newHandle = foldInstructions(ppHandle, pHandle, handle, instList, cpgen, variablesList);
				instructionStack.push(newHandle);
			}	
			
			
			
		}
		
		// setPositions(true) checks whether jump handles 
		// are all within the current method
		instList.setPositions(true);

		// set max stack/local
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// generate the new method with replaced iconst
		Method newMethod = methodGen.getMethod();
		// replace the method in the original class
		cgen.replaceMethod(method, newMethod);

	}

	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		
		// Do your optimization here
		ConstantPoolGen cpgen = cgen.getConstantPool();

		// Do your optimization here
		Method[] methods = cgen.getMethods();
		for (Method m : methods)
		{
			optimizeMethod(cgen, cpgen, m);

		}

		this.optimized = cgen.getJavaClass();
	}
	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}