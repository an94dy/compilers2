package comp2010.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;


public class ConstantFolder
{
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
	
	private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		
		Code methodCode = method.getCode();
		InstructionList instList = new InstructionList(methodCode.getCode());
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);
		
		int prevValue = -1, prevprevValue = -1;
		InstructionHandle pHandle = null, ppHandle = null;

		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			Instruction currentInstruction = handle.getInstruction();
			
			if (currentInstruction instanceof LDC) {
				
				LDC LDCInstruction = (LDC)(currentInstruction);
				
				Object LDCValue = LDCInstruction.getValue(cpgen);
				if (LDCValue instanceof Integer) {
					int value = (int)(LDCValue);
					ppHandle = pHandle;
					pHandle = handle;
					prevprevValue = prevValue;
					prevValue = value;
				}
			}
			else {
				if (pHandle != null) {
					if (currentInstruction instanceof IADD || currentInstruction instanceof ISUB
						|| currentInstruction instanceof IMUL || currentInstruction instanceof IDIV
						|| currentInstruction instanceof IREM) {
						
						int sum = 0;
						
						if (currentInstruction instanceof IADD){
							sum = prevprevValue + prevValue;
						}
						else if (currentInstruction instanceof ISUB){
							sum = prevprevValue - prevValue;
						}
						else if (currentInstruction instanceof IMUL){
							sum = prevprevValue * prevValue;
						}
						else if (currentInstruction instanceof IDIV){
							sum = prevprevValue / prevValue;
						}
						else if (currentInstruction instanceof IREM){
							sum = prevprevValue % prevValue;
						}
						
						
						int newCpIndex = cpgen.addInteger(sum);;

						InstructionHandle newHandle = instList.append(handle, new LDC(newCpIndex));
						
						try {
							instList.delete(pHandle);
							if (ppHandle != null) {
								instList.delete(ppHandle);
							}
							instList.delete(handle);
						}
						catch (TargetLostException e)
						{
							e.printStackTrace();
						}
						
						pHandle = newHandle;
						ppHandle = null;

						prevprevValue = prevValue;
						prevValue = sum;
					}
				}
				
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
