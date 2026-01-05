package catering.businesslogic.holiday;

public interface HolidayEventReceiver {
    void updateHolidayCreated(HolidayRequest hr);
    void updateHolidayManaged(HolidayRequest hr);
}