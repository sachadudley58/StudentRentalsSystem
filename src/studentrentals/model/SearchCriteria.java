package studentrentals.model;

import java.time.LocalDate;

public final class SearchCriteria {
    private String area;
    private Integer minPrice;
    private Integer maxPrice;
    private RoomType roomType;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getArea()
    { 
        return area; 
    }
    public Integer getMinPrice() 
    { 
        return minPrice; 
    }
    public Integer getMaxPrice() 
    { 
        return maxPrice;
    }
    public RoomType getRoomType() 
    { 
        return roomType; 
    }
    public LocalDate getStartDate() 
    { 
        return startDate; 
    }
    public LocalDate getEndDate() 
    { 
        return endDate; 
    }

    public void setArea(String area) 
    { 
        this.area = area; 
    }

    public void setMinPrice(Integer minPrice) 
    { 
        this.minPrice = minPrice; 
    }

    public void setMaxPrice(Integer maxPrice) 
    { 
        this.maxPrice = maxPrice; 
    }

    public void setRoomType(RoomType roomType) 
    { 
        this.roomType = roomType; 
    }

    public void setStartDate(LocalDate startDate) 
    { 
        this.startDate = startDate; 
    }

    public void setEndDate(LocalDate endDate) 
    { 
        this.endDate = endDate; 
    }
}
