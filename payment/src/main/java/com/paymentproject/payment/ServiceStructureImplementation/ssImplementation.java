package com.paymentproject.payment.ServiceStructureImplementation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.paymentproject.payment.Model.customerInfo;
import com.paymentproject.payment.RepositoryLevel.repository;
import com.paymentproject.payment.ServiceStructure.ServiceStructure;

@Service
public class ssImplementation implements ServiceStructure {
    @Autowired
    repository rp;

    BCryptPasswordEncoder BCPE = new BCryptPasswordEncoder(10);

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public customerInfo createUser(customerInfo cs) {
        cs.setPassword(BCPE.encode(cs.getPassword()));
        return rp.save(cs);

    }

    @Override
    @Transactional
    public customerInfo transferMoney(double amount, int ri, int si) {
        customerInfo reciever = rp.getById(ri);
        customerInfo sender = rp.getById(si);

        double presentMoney = reciever.getMoney() + amount;
        double deductedMoney = sender.getMoney() - amount;
        reciever.setMoney(presentMoney);
        sender.setMoney(deductedMoney);
        rp.save(sender);
        return rp.save(reciever);
    }

    @Override
    public customerInfo addMoney(double amount, int id) {
        customerInfo ci = rp.findById(id).get();

        ci.setMoney(ci.getMoney() + amount);
        rp.save(ci);
        return ci;

    }

    @Override
    public void deleteUser(int id) {
        rp.deleteById(id);
    }

    @Override
    public List<customerInfo> getAllInfo() {
        return rp.findAll();
    }

    @Override
    public customerInfo getMydetail(int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        customerInfo c = rp.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
        if (c.getCustomerName().equals(username)) {
            return c;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "please enter valid id");

    }

    @Override
    public customerInfo getUserByUsername(String username) {
        // return rp.findByCustomerName(username)
        // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User
        // not found"));

        customerInfo c = rp.findByCustomerName(username);
        if (c != null) {
            return c;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    @Override
    public List<customerInfo> bulkAddCustomers(List<customerInfo> customers) {
        
        customers.forEach(c -> c.setPassword(BCPE.encode(c.getPassword())));
        return rp.saveAll(customers);
    }

    @Override
    public void saveToRedis(String key, Object value) {
        String redisKey = "customerName:" + key;
        redisTemplate.opsForValue().set(redisKey, value);
    }


    @Override
    public Object getFromRedis(String key) {
        String redisKey = "customerName:" + key;
        return redisTemplate.opsForValue().get(redisKey);
    }

    @Override
    public boolean existsInRedis(String key) {
        String redisKey = "customerName:" + key;
        return redisTemplate.hasKey(redisKey);
    }

    

}
