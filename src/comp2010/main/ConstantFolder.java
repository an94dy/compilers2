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
	
	private static void clearStack (Stack<InstructionHandle> stack) {
		try {
			stack.clear();
		}
		catch (EmptyStackException e) {
			e.printStackTrace();
		}
	}
	
	private static valueType getInstructionValueType (InstructionHandle handle) {
		Instruction instruction = handle.getInstruction();
		
		if (instruction instanceof IADD || 
			instruction instanceof ISUB	||
			instruction instanceof IMUL || 
			instruction instanceof IDIV	|| 
			instruction instanceof IREM ||
			instruction instanceof FCMPG || 
			instruction instanceof FCMPL || 
			instruction instanceof LCMP	|| 
			instruction instanceof DCMPG || 
			instruction instanceof DCMPL ||
			instruction instanceof IAND ||
			instruction instanceof IXOR ||
			instruction instanceof IOR ||
			instruction instanceof ISHR ||
			instruction instanceof ISHL ||
			instruction instanceof IUSHR
			) 
		{
			return valueType.INT;
		}
		else if (instruction instanceof FADD ||
				 instruction instanceof FSUB ||
				 instruction instanceof FMUL ||
				 instruction instanceof FDIV ||
				 instruction instanceof FREM) 
		{
			return valueType.FLOAT;
		}
		else if (instruction instanceof LADD ||
				 instruction instanceof LSUB ||
				 instruction instanceof LMUL ||
				 instruction instanceof LDIV || 
				 instruction instanceof LREM ||
				 instruction instanceof LAND ||
				 instruction instanceof LXOR ||
				 instruction instanceof LOR ||
				 instruction instanceof LSHR ||
				 instruction instanceof LSHL ||
				 instruction instanceof LUSHR
				 ) 
		{
			return valueType.LONG;
		}
		else if (instruction instanceof DADD ||
				 instruction instanceof DSUB ||
				 instruction instanceof DMUL || 
				 instruction instanceof DDIV ||
				 instruction instanceof DREM) 
		{
			return valueType.DOUBLE;
		}
		else {
			return valueType.OTHER;
		}
	}
	
	private static Number getVal (ConstantPoolGen cpgen, InstructionHandle handle) {
		
		Instruction instruction = null;
		try {
			instruction = handle.getInstruction();
		}
		catch (NullPointerException e) {
			
		}
		
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

		return value;
	}
	
	private static Number getResult (Number ppVal, Number pVal, InstructionHandle handle) {
		
		Instruction instruction = handle.getInstruction();
		Number result = 0;
		if (instruction instanceof IfInstruction) {
			int a = ppVal.intValue();
			int b = pVal.intValue();
			
			if (instruction instanceof IF_ICMPEQ || instruction instanceof IFEQ) {
				
				if (a == b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
			else if (instruction instanceof IF_ICMPGE || instruction instanceof IFGE) {
				
				if (a >= b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
			else if (instruction instanceof IF_ICMPGT || instruction instanceof IFGT) {
				
				if (a > b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
			else if (instruction instanceof IF_ICMPLE || instruction instanceof IFLE) {
				
				if (a <= b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
			else if (instruction instanceof IF_ICMPLT || instruction instanceof IFLT) {
				
				if (a < b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
			else if (instruction instanceof IF_ICMPNE || instruction instanceof IFNE) {
				
				if (a != b) {
					result = 0;
				}
				else {
					result = 1;
				}
			}
		}
		else if (instruction instanceof DCMPG ||
			instruction instanceof DCMPL ||
			instruction instanceof FCMPG ||
			instruction instanceof FCMPL ||
			instruction instanceof LCMP) {

			if (instruction instanceof DCMPG ||
				instruction instanceof DCMPL) {
				
				double a = ppVal.doubleValue();
				double b = pVal.doubleValue();
				if (a == b) {
					result = 0;
				}
				else if (a < b) {
					result = -1;
				}
				else if (a > b){
					result = 1;
				}
				else if (instruction instanceof DCMPG){
					result = 1;
				}
				else {
					result = -1;
				}
			}
			else if (instruction instanceof FCMPG ||
					 instruction instanceof FCMPL) {
				float a = ppVal.floatValue();
				float b = pVal.floatValue();
				if (a == b) {
					result = 0;
				}
				else if (a < b) {
					result = -1;
				}
				else if (a > b) {
					result = 1;
				}
				else if (instruction instanceof FCMPG){
					result = 1;
				}
				else {
					result = -1;
				}
			}
			else if (instruction instanceof LCMP) {
				long a = ppVal.longValue();
				long b = pVal.longValue();
				if (a == b) {
					result = 0;
				}
				else if (a < b) {
					result = -1;
				}
				else {
					result = 1;
				}
			}
			
		}
		else {

			valueType type = getInstructionValueType(handle);
			result = 0;
			
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
		}
			
		
		return result;
	}
	
	private static void deleteInstruction (InstructionHandle handle, InstructionList instList) {
		try {
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
		catch (NullPointerException e) {
			
		}
	}
	
	private static InstructionHandle optimizeNegationInstruction(InstructionHandle pHandle, InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
		Instruction newInstruction = null;
		Instruction instruction = handle.getInstruction();
		Number val = getVal(cpgen, pHandle);
		
		if (instruction instanceof INEG) {
			newInstruction = new PUSH(cpgen, -(val.intValue())).getInstruction();
		}
		else if (instruction instanceof FNEG) {
			newInstruction = new PUSH(cpgen, -(val.floatValue())).getInstruction();
		}
		else if (instruction instanceof LNEG) {
			newInstruction = new PUSH(cpgen, -(val.longValue())).getInstruction();
		}
		else if (instruction instanceof DNEG) {
			newInstruction = new PUSH(cpgen, -(val.doubleValue())).getInstruction();
		}
		
		if (newInstruction != null) {
			pHandle.setInstruction(newInstruction);
			try {
				
				InstructionTargeter[] targeters = handle.getTargeters();
				for (InstructionTargeter targeter : targeters) {
					pHandle.addTargeter(targeter);
				}
				instList.redirectBranches(handle, pHandle);
			}
			catch (NullPointerException e) {
			}
			deleteInstruction(handle, instList);
			return pHandle;
		}
		else {
			return null;
		}
		
	}
	
	private static InstructionHandle optimizeConversionInstruction (InstructionHandle pHandle, InstructionHandle handle, ConstantPoolGen cpgen, InstructionList instList) {
		Instruction conversionInstruction = handle.getInstruction();
		Instruction newInstruction = null;
		Number val = getVal(cpgen, pHandle);
		
		if (conversionInstruction instanceof I2F ||
				 conversionInstruction instanceof L2F ||
				 conversionInstruction instanceof D2F) {

			newInstruction = new PUSH(cpgen, val.floatValue()).getInstruction();
		}
		else if (conversionInstruction instanceof I2L ||
				 conversionInstruction instanceof F2L ||
				 conversionInstruction instanceof D2L) {

			newInstruction = new PUSH(cpgen, val.longValue()).getInstruction();
		}
		else if (conversionInstruction instanceof I2D ||
				 conversionInstruction instanceof F2D ||
				 conversionInstruction instanceof L2D) {

			newInstruction = new PUSH(cpgen, val.doubleValue()).getInstruction();
		}
		else {
			newInstruction = new PUSH(cpgen, val.intValue()).getInstruction();
		}
		
		if (newInstruction != null) {
			pHandle.setInstruction(newInstruction);
			try {
				InstructionTargeter[] targeters = handle.getTargeters();
				for (InstructionTargeter targeter : targeters) {
					pHandle.addTargeter(targeter);
				}
				instList.redirectBranches(handle, pHandle);
			}
			catch (NullPointerException e) {
				
			}
			deleteInstruction(handle, instList);
			return pHandle;
		}
		else {
			return null;
		}
		
		
	}
		
	private static InstructionHandle optimizeArithmeticInstruction (InstructionHandle ppHandle, InstructionHandle pHandle, InstructionHandle handle,
					InstructionList instList, ConstantPoolGen cpgen) {
	
		Instruction newInstruction;
		valueType type = getInstructionValueType(handle);
		
		Number pVal = getVal(cpgen, pHandle);
		Number ppVal = getVal(cpgen, ppHandle);
		Number result = getResult(ppVal, pVal, handle);
		
		switch (type) {
			case INT:
				/*int val = result.intValue();
				if (val >= -1 && val <= 5) {
					newInstruction = instList.append(handle, new ICONST((byte)(val)));
				}
				else if (val >= -128 && val <= 127) {
					newInstruction = instList.append(handle, new BIPUSH((byte)(val)));
				}
				else if (val >= -32768 && val <= 32767) {
					newInstruction = instList.append(handle, new SIPUSH((short)(val)));
				}
				else {
					newCpIndex = cpgen.addInteger(val);
					newInstruction = instList.append(handle, new LDC(newCpIndex));
				}*/
				newInstruction = new PUSH(cpgen, result.intValue()).getInstruction();
				break;
				
			case FLOAT:
				//newCpIndex = cpgen.addFloat(result.floatValue());;
				//newInstruction = instList.append(handle, new LDC_W(newCpIndex));
				newInstruction = new PUSH(cpgen, result.floatValue()).getInstruction();
				break;
				
			case LONG:
				//newCpIndex = cpgen.addLong(result.longValue());
				//newInstruction = instList.append(handle, new LDC2_W(newCpIndex));
				newInstruction = new PUSH(cpgen, result.longValue()).getInstruction();
				break;
				
			case DOUBLE:
				//newCpIndex = cpgen.addDouble(result.doubleValue());
				//newInstruction = instList.append(handle, new LDC2_W(newCpIndex));
				newInstruction = new PUSH(cpgen, result.doubleValue()).getInstruction();
				break;
				
			default:
				newInstruction = null;
				break;
		}
		if (newInstruction != null) {
			ppHandle.setInstruction(newInstruction);
			try {
				InstructionTargeter[] handleTargeters = handle.getTargeters();
				InstructionTargeter[] pHandleTargeters = pHandle.getTargeters();
	
				for (InstructionTargeter targeter : handleTargeters) {
					ppHandle.addTargeter(targeter);
				}
				for (InstructionTargeter targeter : pHandleTargeters) {
					ppHandle.addTargeter(targeter);
				}
				
				instList.redirectBranches(pHandle, ppHandle);
				instList.redirectBranches(handle, ppHandle);
			}
			catch (NullPointerException e) {
			}
			deleteInstruction(handle, instList);
			deleteInstruction(pHandle, instList);
			return ppHandle;
			
		}
		else {
			return null;
		}
	}

	private static InstructionHandle optimizeIfInstruction(InstructionHandle ppHandle, InstructionHandle pHandle, InstructionHandle handle, ConstantPoolGen cpgen, InstructionList instList) {
		IfInstruction ifInstruction = (IfInstruction)(handle.getInstruction());
		InstructionHandle target = ifInstruction.getTarget();
		
		Number pVal = 0;
		Number ppVal = getVal(cpgen, pHandle);
		
		if (ppHandle != null) {
			ppVal = getVal(cpgen, ppHandle);
			pVal = getVal(cpgen, pHandle);
		}
		
		Number result = getResult(ppVal, pVal, handle);
		int resultVal = result.intValue();
		if (ppHandle != null) {
			deleteInstruction(ppHandle, instList);
		}
		
		deleteInstruction(pHandle, instList);
		deleteInstruction(handle, instList);
		
		if (resultVal == 0) {
			return target;
		}
		else {
			return null;
		}
		
	}
	
	private static InstructionHandle optimizeGoToInstruction(InstructionHandle handle, ConstantPoolGen cpgen, InstructionList instList) {
		GOTO goToInstruction = (GOTO)(handle.getInstruction());
		InstructionHandle target = goToInstruction.getTarget();
		deleteInstruction(handle, instList);
		return target;
		
	}
	
	
	
	private void simpleFold (ConstantPoolGen cpgen, InstructionList instList) {
		
		Stack<InstructionHandle> instructionStack = new Stack<InstructionHandle>();
		InstructionHandle[] instructionHandles = instList.getInstructionHandles();
		int length = instructionHandles.length;
		int k =0;
		
		InstructionHandle removeUntil = null;
		boolean gotoAfterIf = false;
		
		for (k = 0; k < length; k++)
		{
			
			InstructionHandle handle = instructionHandles[k];
			if (removeUntil != null) {
				if (handle == removeUntil) {
					removeUntil = null;
				}
				else {
					deleteInstruction(handle, instList);
				}
			}
			
			if (removeUntil == null) {
				Instruction currentInstruction = handle.getInstruction();
				
				if (currentInstruction instanceof LDC || 
					currentInstruction instanceof LDC_W || 
					currentInstruction instanceof LDC2_W || 
					currentInstruction instanceof ICONST ||
					currentInstruction instanceof FCONST ||
					currentInstruction instanceof LCONST ||
					currentInstruction instanceof DCONST ||
					currentInstruction instanceof BIPUSH ||
					currentInstruction instanceof SIPUSH
					) 
				{
					instructionStack.push(handle);
				}
				else if (currentInstruction instanceof IfInstruction && instructionStack.size() > 0) {
					
					InstructionHandle pHandle = null, ppHandle = null;
					if (
						(
						currentInstruction instanceof IF_ACMPEQ 
						|| currentInstruction instanceof IF_ACMPNE
						|| currentInstruction instanceof IF_ICMPEQ
						|| currentInstruction instanceof IF_ICMPGE
						|| currentInstruction instanceof IF_ICMPGT
						|| currentInstruction instanceof IF_ICMPLE
						|| currentInstruction instanceof IF_ICMPLT
						|| currentInstruction instanceof IF_ICMPNE
						)
						&& instructionStack.size() > 1) 
					{
						
						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
							ppHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}	
					}
					else if (
							currentInstruction instanceof IFEQ 
							|| currentInstruction instanceof IFGE
							|| currentInstruction instanceof IFGT
							|| currentInstruction instanceof IFLE
							|| currentInstruction instanceof IFLT
							|| currentInstruction instanceof IFNE
							|| currentInstruction instanceof IFNONNULL
							|| currentInstruction instanceof IFNULL
							)
					{
						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}
					}
					if (pHandle != null && ppHandle != null) {
						if (
								!( 
								currentInstruction instanceof IF_ACMPEQ
								|| currentInstruction instanceof IF_ACMPNE
								|| currentInstruction instanceof IFNONNULL
								|| currentInstruction instanceof IFNULL
								)
							) 
						{	
							removeUntil = optimizeIfInstruction(ppHandle, pHandle, handle, cpgen, instList);
							gotoAfterIf = true;
						}		
					}
					else {
						clearStack(instructionStack);
					}
					
				}
				else if (currentInstruction instanceof GOTO && gotoAfterIf) {
					removeUntil = optimizeGoToInstruction(handle, cpgen, instList);
				}
				else if (currentInstruction instanceof ConversionInstruction && instructionStack.size() > 0) {
					InstructionHandle pHandle = null;
					try {
						pHandle = (InstructionHandle)(instructionStack.pop());
					}
					catch (EmptyStackException e) {
						e.printStackTrace();
						continue;
					}
					
					InstructionHandle newHandle = optimizeConversionInstruction(pHandle, handle, cpgen, instList);
					if (newHandle != null) {
						instructionStack.push(newHandle);
					}
				}
				else if (
						(currentInstruction instanceof ArithmeticInstruction 
						|| currentInstruction instanceof FCMPG 
						|| currentInstruction instanceof FCMPL 
						|| currentInstruction instanceof LCMP 
						|| currentInstruction instanceof DCMPG 
						|| currentInstruction instanceof DCMPL)
						&& instructionStack.size() > 0) {
					
					InstructionHandle pHandle = null, ppHandle = null;
					if (
						(currentInstruction instanceof INEG 
						|| currentInstruction instanceof FNEG
						|| currentInstruction instanceof LNEG 
						|| currentInstruction instanceof DNEG) 
						&& instructionStack.size() > 0
						) 
					{
						
						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}
						InstructionHandle newHandle = optimizeNegationInstruction(pHandle, handle, instList, cpgen);
						if (newHandle != null) {
							instructionStack.push(newHandle);
						}
					}
					else if (instructionStack.size() > 1) {

						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
							ppHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}
						InstructionHandle newHandle = optimizeArithmeticInstruction(ppHandle, pHandle, handle, instList, cpgen);
						if (newHandle != null) {
							instructionStack.push(newHandle);
						}
					}
					else {
						clearStack(instructionStack);
					}
				}
				else if (currentInstruction instanceof StackProducer || currentInstruction instanceof StackConsumer) {
					clearStack(instructionStack);
				}
			}
		}
	}

	
	
	
	private static void storeVariable (InstructionHandle pHandle, InstructionHandle handle, ConstantPoolGen cpgen, HashMap<Integer,Number> variablesList) {
		Instruction instruction = handle.getInstruction();
		
		if (instruction instanceof ISTORE) {
			ISTORE ISTOREInstruction = (ISTORE)(instruction);
			int index = ISTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle);
			variablesList.put(index, value.intValue());
		}
		else if (instruction instanceof FSTORE) {
			FSTORE FSTOREInstruction = (FSTORE)(instruction);
			int index = FSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle);
			variablesList.put(index, value.floatValue());
		}
		else if (instruction instanceof LSTORE) {
			LSTORE LSTOREInstruction = (LSTORE)(instruction);
			int index = LSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle);
			variablesList.put(index, value.longValue());
		}
		else if (instruction instanceof DSTORE) {
			DSTORE DSTOREInstruction = (DSTORE)(instruction);
			int index = DSTOREInstruction.getIndex();
			Number value = getVal(cpgen, pHandle);
			variablesList.put(index, value.doubleValue());
		}
	}
	
	private static void incrementIntegerVariable (InstructionHandle handle, HashMap<Integer,Number> variablesList) {
		Instruction instruction = handle.getInstruction();
		int index = ((IINC)(instruction)).getIndex();
		int incrementValue = ((IINC)(instruction)).getIncrement();
		try {
			int value = (int)(variablesList.get(index));
			int newVal = value + incrementValue;
			variablesList.put(index, newVal);
		}
		catch (NullPointerException e) {
		}
		//handle.setInstruction();
	}
	
	private static InstructionHandle replaceLoadInstruction (InstructionHandle handle, ConstantPoolGen cpgen, InstructionList instList, HashMap<Integer,Number> variablesList) {
		Instruction newInstruction = null;
		Instruction instruction = handle.getInstruction();
		Number value = 0;
		
		try {
			if (instruction instanceof ALOAD) {
				
			}
			else { 
				if (instruction instanceof ILOAD) {
					ILOAD ILOADInstruction = (ILOAD)(instruction);
					int index = ILOADInstruction.getIndex();
					Number ILOADValue = variablesList.get(index);
					value = ILOADValue.intValue();
					newInstruction = new PUSH(cpgen, value.intValue()).getInstruction();
				}
				else if (instruction instanceof FLOAD) {
					FLOAD FLOADInstruction = (FLOAD)(instruction);
					int index = FLOADInstruction.getIndex();
					Number FLOADValue = variablesList.get(index);
					value = FLOADValue.floatValue();
					newInstruction = new PUSH(cpgen, value.floatValue()).getInstruction();
				}
				else if (instruction instanceof LLOAD) {
					LLOAD LLOADInstruction = (LLOAD)(instruction);
					int index = LLOADInstruction.getIndex();
					Number LLOADValue = variablesList.get(index);
					value = LLOADValue.longValue();
					newInstruction = new PUSH(cpgen, value.longValue()).getInstruction();
				}
				else if (instruction instanceof DLOAD) {
					DLOAD DLOADInstruction = (DLOAD)(instruction);
					int index = DLOADInstruction.getIndex();
					Number DLOADValue = variablesList.get(index);
					value = DLOADValue.doubleValue();
					newInstruction = new PUSH(cpgen, value.doubleValue()).getInstruction();
				}
			}
		}
		catch (NullPointerException e) {
		}
		
		if (newInstruction != null) {
			handle.setInstruction(newInstruction);
			return handle;
		}
		else {
			return null;
		}
	}
	
	private void constantVariableFold (ConstantPoolGen cpgen, InstructionList instList){
		
		HashMap<Integer,Number> variablesList = new HashMap<Integer,Number>();
		Stack<InstructionHandle> instructionStack = new Stack<InstructionHandle>();
		InstructionHandle[] instructionHandles = instList.getInstructionHandles();
		int length = instructionHandles.length;
		int k = 0;
		
		InstructionHandle removeUntil = null;
		boolean gotoAfterIf = false;
		
		for (k = 0; k < length; k++)
		{
			
			InstructionHandle handle = instructionHandles[k];
			if (removeUntil != null) {
				if (handle == removeUntil) {
					removeUntil = null;
				}
				else {
					deleteInstruction(handle, instList);
				}
			}
			
			if (removeUntil == null) {
				Instruction currentInstruction = handle.getInstruction();
				
				if (currentInstruction instanceof LDC || 
					currentInstruction instanceof LDC_W || 
					currentInstruction instanceof LDC2_W || 
					currentInstruction instanceof ICONST ||
					currentInstruction instanceof FCONST ||
					currentInstruction instanceof LCONST ||
					currentInstruction instanceof DCONST ||
					currentInstruction instanceof BIPUSH ||
					currentInstruction instanceof SIPUSH
					) 
				{
					instructionStack.push(handle);
				}
				else if (currentInstruction instanceof ConversionInstruction && instructionStack.size() > 0) {
					InstructionHandle pHandle = null;
					try {
						pHandle = (InstructionHandle)(instructionStack.pop());
					}
					catch (EmptyStackException e) {
						e.printStackTrace();
					}
					
					InstructionHandle newHandle = optimizeConversionInstruction(pHandle, handle, cpgen, instList);
					if (newHandle != null) {
						instructionStack.push(newHandle);
					}
				}
				else if (
						(currentInstruction instanceof INEG 
						|| currentInstruction instanceof FNEG
						|| currentInstruction instanceof LNEG 
						|| currentInstruction instanceof DNEG) 
						&& instructionStack.size() > 0) {
					
					InstructionHandle pHandle = null;
					try {
						pHandle = (InstructionHandle)(instructionStack.pop());
					}
					catch (EmptyStackException e) {
						e.printStackTrace();
						continue;
					}
					InstructionHandle newHandle = optimizeNegationInstruction(pHandle, handle, instList, cpgen);
					if (newHandle != null) {
						instructionStack.push(newHandle);
					}	
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
					if (!(currentInstruction instanceof ASTORE)) {
						storeVariable(pHandle, handle, cpgen, variablesList);
						deleteInstruction(handle, instList);
						deleteInstruction(pHandle, instList);
					}
					
				}
				else if (currentInstruction instanceof IINC && !variablesList.isEmpty()) {
					incrementIntegerVariable(handle, variablesList);
				}
				else if (currentInstruction instanceof LoadInstruction && !variablesList.isEmpty()) {
					if (!(currentInstruction instanceof ALOAD)) {
						InstructionHandle newHandle = replaceLoadInstruction (handle, cpgen, instList, variablesList);
						if (newHandle != null) {
							instructionStack.push(newHandle);
						}
					}
				}
				else if (currentInstruction instanceof ArithmeticInstruction ||
						 currentInstruction instanceof FCMPG ||
						 currentInstruction instanceof FCMPL ||
						 currentInstruction instanceof LCMP ||
						 currentInstruction instanceof DCMPG ||
						 currentInstruction instanceof DCMPL) {
					
					InstructionHandle pHandle = null, ppHandle = null;

					if (instructionStack.size() > 1) {

						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
							ppHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}
						InstructionHandle newHandle = optimizeArithmeticInstruction(ppHandle, pHandle, handle, instList, cpgen);
						if (newHandle != null) {
							instructionStack.push(newHandle);
						}
					}
				}
				else if (currentInstruction instanceof IfInstruction && instructionStack.size() > 0) {
					
					InstructionHandle pHandle = null, ppHandle = null;
					if (
						(
						currentInstruction instanceof IF_ACMPEQ 
						|| currentInstruction instanceof IF_ACMPNE
						|| currentInstruction instanceof IF_ICMPEQ
						|| currentInstruction instanceof IF_ICMPGE
						|| currentInstruction instanceof IF_ICMPGT
						|| currentInstruction instanceof IF_ICMPLE
						|| currentInstruction instanceof IF_ICMPLT
						|| currentInstruction instanceof IF_ICMPNE
						)
						&& instructionStack.size() > 1) 
					{
						
						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
							ppHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}	
					}
					else if (
							currentInstruction instanceof IFEQ 
							|| currentInstruction instanceof IFGE
							|| currentInstruction instanceof IFGT
							|| currentInstruction instanceof IFLE
							|| currentInstruction instanceof IFLT
							|| currentInstruction instanceof IFNE
							|| currentInstruction instanceof IFNONNULL
							|| currentInstruction instanceof IFNULL
							)
					{
						try {
							pHandle = (InstructionHandle)(instructionStack.pop());
						}
						catch (EmptyStackException e) {
							e.printStackTrace();
							continue;
						}
					}
					
					if (pHandle != null && ppHandle != null)
					{
						if (
								!( 
								currentInstruction instanceof IF_ACMPEQ
								|| currentInstruction instanceof IF_ACMPNE
								|| currentInstruction instanceof IFNONNULL
								|| currentInstruction instanceof IFNULL
								)
							) 
						{	
							removeUntil = optimizeIfInstruction(ppHandle, pHandle, handle, cpgen, instList);
							gotoAfterIf = true;
						}		
					}
					else {
						clearStack(instructionStack);
					}
				}
				else if (currentInstruction instanceof GOTO && gotoAfterIf) {
					removeUntil = optimizeGoToInstruction(handle, cpgen, instList);
				}
				else if (currentInstruction instanceof StackProducer || currentInstruction instanceof StackConsumer) {
					clearStack(instructionStack);
				}
			}
		}
	}
	
	
	
	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		
		Code methodCode = method.getCode();
		InstructionList instList = new InstructionList(methodCode.getCode());
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);
	
		//simpleFold(cpgen, instList);
		constantVariableFold(cpgen, instList);

		
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