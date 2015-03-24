package xtream.usecase;

import xtream.core.loadshedding.LSResponseTimeContainer;

public class LSResponseTimeContainer_Usecase {

	public static void main(String[] args) {
		LSResponseTimeContainer rt = new LSResponseTimeContainer(8);
		System.out.println("\nBegin");
		System.out.println(rt);
		
		rt.AddValue(0.01, 1000);
		rt.AddValue(0.02, 2000);
		rt.AddValue(0.03, 4000);
		rt.AddValue(0.3, 5000);
		rt.AddValue(0.6, 7000);
		rt.AddValue(1, 1000);
		rt.AddValue(0, 9000);

		System.out.println("\nAfter Insertion");
		System.out.println(rt);
		
		System.out.println("\nGlobal RT: "+rt.GetRT());
		
		System.out.println("\nRelease of 0.5: "+rt.getRTRelease(0.5));
		
		rt.SetPT(0.3);
		
		System.out.println("\nPT changed... to 0.3");
		System.out.println("\nRelease of 1: "+rt.getRTRelease(1));




	}

}
