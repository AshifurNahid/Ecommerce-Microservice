package com.nahid.customer.mapper;

import org.mapstruct.*;
import com.nahid.customer.dto.AddressDto;
import com.nahid.customer.dto.CustomerRequestDto;
import com.nahid.customer.dto.CustomerResponseDto;
import com.nahid.customer.entity.Customer;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)


public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "addressDtoListToAddressList")
    Customer toEntity(CustomerRequestDto dto);

    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "addressListToAddressDtoList")
    CustomerResponseDto toResponseDto(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "addressDtoListToAddressList")
    void updateEntityFromDto(CustomerRequestDto dto, @MappingTarget Customer entity);

    @Named("addressDtoListToAddressList")
    default List<Customer.Address> addressDtoListToAddressList(List<AddressDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::addressDtoToAddress)
                .toList();
    }

    @Named("addressListToAddressDtoList")
    default List<AddressDto> addressListToAddressDtoList(List<Customer.Address> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::addressToAddressDto)
                .toList();
    }

    @Mapping(target = "id", expression = "java(dto.getId() != null ? dto.getId() : java.util.UUID.randomUUID().toString())")
    Customer.Address addressDtoToAddress(AddressDto dto);

    AddressDto addressToAddressDto(Customer.Address entity);
}
