package es.ual.dra.autodiagnostico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import es.ual.dra.autodiagnostico.service.UltimateSpecsVehicleScraperService;

@SpringBootApplication
public class AutoDiagnosticoApplication {

	public static void main(String[] args) {
		// SpringApplication.run(AutoDiagnosticoApplication.class, args);
		ApplicationContext context = SpringApplication.run(AutoDiagnosticoApplication.class, args);
		UltimateSpecsVehicleScraperService vss = context.getBean(UltimateSpecsVehicleScraperService.class);
		try {
			vss.scrapeAndSave();
			System.out.println("NO ERROR");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
	}

}
