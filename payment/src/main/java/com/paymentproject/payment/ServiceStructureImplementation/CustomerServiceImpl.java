package com.paymentproject.payment.ServiceStructureImplementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.paymentproject.payment.Model.CustomerInfo;
import com.paymentproject.payment.RepositoryLevel.CustomerRepository;
import com.paymentproject.payment.ServiceStructure.ServiceStructure;

@Service
public class CustomerServiceImpl implements ServiceStructure {
    @Autowired
    CustomerRepository customerRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public CustomerInfo createUser(CustomerInfo customer) {
        customer.setPassword(bCryptPasswordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);

    }

    @Override
    @Transactional
    public CustomerInfo transferMoney(double amount, int receiverId, int senderId) {
        CustomerInfo receiver = customerRepository.getById(receiverId);
        CustomerInfo sender = customerRepository.getById(senderId);
        if (sender.getMoney() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
        }

        double receiverBalance = receiver.getMoney() + amount;
        double senderBalance = sender.getMoney() - amount;
        receiver.setMoney(receiverBalance);
        sender.setMoney(senderBalance);
        customerRepository.save(sender);
        return customerRepository.save(receiver);
    }

    @Override
    public CustomerInfo addMoney(double amount, int id) {
        CustomerInfo customer = customerRepository.findById(id).get();

        customer.setMoney(customer.getMoney() + amount);
        customerRepository.save(customer);
        return customer;

    }

    @Override
    public void deleteUser(int id) {
        customerRepository.deleteById(id);
    }

    @Override
    public List<CustomerInfo> getAllInfo() {
        return customerRepository.findAll();
    }

    @Override
    @Cacheable(key = "#id", value = "customerInfo")
    public CustomerInfo getMyDetails(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        CustomerInfo customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        if (customer.getCustomerName().equals(username)) {
            return customer;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "please enter valid id");

    }

    @Override
    @Cacheable(key = "#username", value = "customerInfo")
    public CustomerInfo getUserByUsername(String username) {
        // return customerRepository.findByCustomerName(username)
        // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User
        // not found"));

        CustomerInfo customer = customerRepository.findByCustomerName(username);
        if (customer != null) {
            return customer;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    @Override
    public List<CustomerInfo> bulkAddCustomers(List<CustomerInfo> customers) {

        customers.forEach(c -> c.setPassword(bCryptPasswordEncoder.encode(c.getPassword())));
        return customerRepository.saveAll(customers);
    }

    // @Override
    // public void saveToRedis(String key, Object value) {
    // String redisKey = "CustomerName:" + key;
    // redisTemplate.opsForValue().set(redisKey, value);
    // }

    // @Override
    // public Object getFromRedis(String key) {
    // String redisKey = "CustomerName:" + key;
    // return redisTemplate.opsForValue().get(redisKey);
    // }

    // @Override
    // public boolean existsInRedis(String key) {
    // String redisKey = "CustomerName:" + key;
    // return redisTemplate.hasKey(redisKey);
    // }

}
