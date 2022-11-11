package com.project.auction.model;

import com.project.auction.model.relation.AuctionOffer;
import com.project.auction.util.Utils;
import lombok.Data;

import javax.persistence.*;
import java.util.*;

@Data
@Entity
@Table(name = "item")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", length = 80, nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "finish_date", nullable = false)
    private Date finishDate;

    @Column(name = "start_price")
    private int startPrice;

    @Column(name = "min_next_offer")
    private int minNextOffer;

    @Column(name = "is_finalized")
    private boolean isFinalized = false;
//
//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "location_id", nullable = false)
//    private Location locationId;

    @Column(name = "physical_payment")
    private boolean physicalPayment;

    @Column(name = "virtual_payment")
    private boolean virtualPayment;

    @OneToMany(mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Collection<AuctionOffer> auctionOffers;

    @OneToMany(mappedBy = "item",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Collection<ItemImage> itemImages;

//    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
//    @JoinTable(
//            name = "item_category",
//            joinColumns = @JoinColumn(
//                    name = "item_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "category_id", referencedColumnName = "id")
//
//    )
//    private Collection<Category> categories;

    public void setImages(Collection<ItemImage> itemImages) {
        HashSet<ItemImage> newItemImages = new HashSet<>();
        for (ItemImage itemImage : itemImages) {
            itemImage.setItem(this);
            newItemImages.add(itemImage);
        }
        this.itemImages = newItemImages;
    }

    public boolean isEnabled() {
        Date currentDate = new Date();
        return (currentDate.after(startDate) && currentDate.before(finishDate)) && !isFinalized;
    }

    public List<Person> getParticipants() {
        List<Person> listPersons = new ArrayList<>();
        for(AuctionOffer auctionOffer : this.auctionOffers) {
            Person person = auctionOffer.getPerson();
            if(!listPersons.contains(person)) {
                listPersons.add(person);
            }
        }
        return listPersons;
    }

    public List<AuctionOffer> getOffersInOrder() {
        List<AuctionOffer> ordOffers = (List<AuctionOffer>) this.auctionOffers;
        Comparator<AuctionOffer> mostOfferComparator = ((o1, o2) -> Integer.compare(o2.getOffer(), o1.getOffer()));
        ordOffers.sort(mostOfferComparator);
        return ordOffers;
    }

    public AuctionOffer getMostOffer() {
        if (getOffersInOrder().size() > 0)
            return getOffersInOrder().get(0);
        return null;
    }

    public String getTimeToStart() {
        Date currentDate = new Date();
        long millis = 0;
        if(currentDate.before(this.startDate)) {
            millis = this.startDate.getTime() - currentDate.getTime();
        }
        return Utils.formatTime(millis);
    }

    public String getTimeToFinish() {
        Date currentDate = new Date();
        long millis = 0;
        if(currentDate.before(this.finishDate)) {
            millis = this.finishDate.getTime() - currentDate.getTime();
        }
        return Utils.formatTime(millis);
    }


}