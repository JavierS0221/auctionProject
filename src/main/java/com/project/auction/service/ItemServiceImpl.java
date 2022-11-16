package com.project.auction.service;

import com.project.auction.dto.ItemDto;
import com.project.auction.email.context.AccountVerificationEmailContext;
import com.project.auction.model.ItemImage;
import com.project.auction.model.Person;
import com.project.auction.model.relation.AuctionOffer;
import com.project.auction.repository.ItemRepository;
import com.project.auction.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Item> listItems() {
        return (List<Item>) itemRepository.findAll();
    }

    @Override
    @Transactional
    public long save(Item item) {
        return itemRepository.save(item).getId();
    }

    @Override
    @Transactional
    public long save(ItemDto itemDto) {
        Item item = getItem(itemDto);
        if (item != null) {
            return itemRepository.save(item).getId();

        }
        return 0;
    }

    @Override
    @Transactional
    public void delete(Item item) {
        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItem(Item item) {
        return itemRepository.findById(item.getId()).orElse(null);
    }

    public Item getItem(ItemDto itemDto) {
        if (itemDto == null) return null;
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setPerson(itemDto.getPerson());
        item.setImages(itemDto.getItemImages());
        item.setAuctionOffers(itemDto.getAuctionOffers());
        item.setStartDate(itemDto.getStartDate());
        item.setFinishDate(itemDto.getFinishDate());
//        item.setCategories(itemDto.getCategories());
        item.setMinNextOffer(itemDto.getMinNextOffer());
        item.setStartPrice(itemDto.getStartPrice());
//        item.setLocationId(itemDto.getLocationId());
        item.setPhysicalPayment(itemDto.isPhysicalPayment());
        item.setVirtualPayment(itemDto.isVirtualPayment());
        item.setFinalized(itemDto.isFinalized());
        return item;
    }

    @Override
    public Page<Item> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return this.itemRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItemById(long id) {
        return itemRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void setFinalized(Item item, boolean finalized) {
        item = this.getItem(item);
        if (item != null) {
            item.setFinalized(finalized);
            itemRepository.save(item);
        }
    }


    @Override
    public void sendEmails(Item item) {
        item = this.getItem(item);
        if (item != null) {
            AuctionOffer mostOffer = item.getMostOffer();
                if(mostOffer != null) {

                    Person personWin = mostOffer.getPerson();

                    for (Person person : item.getParticipants()) {
                        if(person == personWin) continue;

                        AccountVerificationEmailContext emailContext = new AccountVerificationEmailContext();
                        emailContext.init(person);
                        emailContext.setToken(secureToken.getToken());
                        emailContext.buildVerificationUrl(baseURL, secureToken.getToken());

                        if (!projectTestingMode) {
                            try {
                                emailService.sendMail(emailContext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                } else {
                    // nadie participo
                }
        }
    }
}
