package comp2010.target;


public class DynamicVariableFolding {
    public int methodOne() {
        int a = 42;
        int b = (a + 764) * 3;
        a = 22;
        return b + 1234 - a;
    }

    public double methodTwo() {
        double x = 12345;
        double y = 54321;
        System.out.println(x < y);
        y = 0;
        return x + y;
    }

    public int methodThree() {
        int i = 0;
        int j = i + 3;
        i = j + 4;
        j = i + 5;
        return i * j;
    }
    
    public int optimiseMe () {
		int a = 534245;
		int b = a - 1234;
		
		System.out.println((120295 - a)*38.435792873);

		// t h e r e t u r n v a l u e can be f o l d e d i n t o a c o n s t a n t
		return a * b ;
	}

	public int methodFour() {
		int a = 10;
		int b = 20;
		
		int c = a + b ;
		int d = a - b;

		return b * d;
		// t h e r e t u r n v a l u e can be f o l d e d i n t o a c o n s t a n t
	}
}
