package hello;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import com.ethlo.dachs.AuditEntityListener;
import com.ethlo.dachs.PropertyChange;
import com.ethlo.dachs.hibernate.HibernateInjector;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}
	
	@Bean
	public Object setupDachs(EntityManagerFactory emf)
	{
		HibernateInjector.registerListeners(emf, new AuditEntityListener()
		{
			@Override
			public void update(Serializable key, Object entity, Collection<PropertyChange<?>> properties) {
				System.out.println("update " + key);
			}
			
			@Override
			public void markUndeleted(Serializable key, Object entity) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void markDeleted(Serializable key, Object entity) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void delete(Serializable key, Object entity) {
				System.out.println("delete ***");
			}
			
			@Override
			public void create(Serializable key, Object entity, Collection<PropertyChange<?>> properties) {
				System.out.println("Created "+ key + " - " + entity + " - " + StringUtils.collectionToCommaDelimitedString(properties));			}
		});
		System.out.println("Registered listener");
		return new Object();
	}

	@Bean
	public CommandLineRunner demo(CustomerRepository repository) {
		return (args) -> {
			// save a couple of customers
			repository.save(new Customer("Jack", "Bauer"));
			repository.save(new Customer("Chloe", "O'Brian"));
			repository.save(new Customer("Kim", "Bauer"));
			repository.save(new Customer("David", "Palmer"));
			repository.save(new Customer("Michelle", "Dessler"));

			// fetch all customers
			log.info("Customers found with findAll():");
			log.info("-------------------------------");
			for (Customer customer : repository.findAll()) {
				log.info(customer.toString());
			}
            log.info("");

			// fetch an individual customer by ID
			Customer customer = repository.findOne(1L);
			log.info("Customer found with findOne(1L):");
			log.info("--------------------------------");
			log.info(customer.toString());
            log.info("");

			// fetch customers by last name
			log.info("Customer found with findByLastName('Bauer'):");
			log.info("--------------------------------------------");
			for (Customer bauer : repository.findByLastName("Bauer")) {
				log.info(bauer.toString());
			}
            log.info("");
		};
	}

}
