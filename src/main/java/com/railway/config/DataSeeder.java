package com.railway.config;

import com.railway.model.Train;
import com.railway.model.User;
import com.railway.repository.TrainRepository;
import com.railway.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository, TrainRepository trainRepository) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(new User("admin", "admin123", "ADMIN"));
            }
            if (userRepository.findByUsername("traveler").isEmpty()) {
                userRepository.save(new User("traveler", "welcome123", "USER"));
            }

            if (trainRepository.count() == 0) {
                trainRepository.save(new Train("12002", "Bhopal Shatabdi"));
                trainRepository.save(new Train("12952", "Mumbai Rajdhani"));
                trainRepository.save(new Train("12301", "Howrah Rajdhani"));
                trainRepository.save(new Train("12627", "Karnataka Express"));
                trainRepository.save(new Train("22435", "Jabalpur Express"));
                trainRepository.save(new Train("20801", "Patna Superfast"));
            }
        };
    }
}
