package xtream.usecase;

import java.io.IOException;

import xtream.core.commonconfig.*;
import xtream.core.log.XLogger;

public class CommonConfig_Usecase {

	public CommonConfig_Usecase() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		System.out.println("Testing CommonConfig:");
		
		try {
			XLogger.setup();

			CommonConfig.Initialize("XConfig.txt");
			System.out.println("Item1: "+ CommonConfig.GetConfigStrItem("item1"));
			
			System.out.println("Press Enter to finish:");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
