package com.suntek.vdm.gw.common.pojo.response.room;

import com.suntek.vdm.gw.common.pojo.response.Pageable;
import com.suntek.vdm.gw.common.pojo.response.Sort;
import lombok.Data;

import java.util.List;

@Data
public class GetAddressBookRoomsComnditionsResp {
    private List<AddressBook> content;
    private Integer totalPages;
    private Integer totalElements;
    private Boolean last;
    private Boolean first;
    private Integer size;
    private Integer number;
    private Boolean empty;
    private Integer numberOfElements;
    private Sort sort;
    private Pageable pageable;
}
