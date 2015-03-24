package xtream.usecase;

import xtream.structures.PeriodicStatistics;

public class PeriodicStatistics_Usecase {

	public PeriodicStatistics_Usecase() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
			PeriodicStatistics pstat = new PeriodicStatistics(1000);
			pstat.newValue(1000, 3000);
			System.out.println(pstat.getSum());
			Thread.sleep(1000);
			System.out.println(pstat.getSum());
			pstat.newValue(5000, 6000);
			System.out.println(pstat.getSum());

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
