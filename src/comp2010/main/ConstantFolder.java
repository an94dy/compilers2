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
		

		int prevPrevHandle = 0, prevHandle = 0;
		int prevValue = 0, prevPrevValue = 0;
		
		for (InstructionHandle handle : instList.getInstructionHandles())
		{
			Instruction currentInstruction = handle.getInstruction();
			if (currentInstruction instanceof LDC) 
			{
				prevPrevHandle = prevHandle;
				prevHandle = handle.getPosition();
				prevPrevValue = prevValue;
				prevValue = ((LDC)(currentInstruction)).getIndex();
			
			}

			if (currentInstruction instanceof ArithmeticInstruction)
			{			
			
				//instList.insert(handle, new LDC(index));
				
				//String as = cpgen.getConstant(prevValue).toString();
				
				ConstantInteger ci = (ConstantInteger)cpgen.getConstant(prevValue);
				//int as = ci.getBytes();
				
				//cpgen.addInteger(as);
				try
				{
					instList.delete(handle);
					instList.delete(instList.findHandle(prevHandle));
					//instList.delete(prev);
					//instList.delete(prevPrev);
				}
				catch (TargetLostException e)
				{
					e.printStackTrace();
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
