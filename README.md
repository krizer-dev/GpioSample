# GpioSample
크라이저 GPIO 샘플 소스

## 📁 구조 설명

- 샘플 소스 (모듈) - sample
- 라이브러리 및 샘플 APK - out

## 🏄‍♀️ 주요 기능

- GPIO READ / WRITE

## 🛠 MANUAL
### GPIO 제어 생성
1. 생성

![Image](https://github.com/user-attachments/assets/985931f2-c227-4afe-b1e8-7e3ce1ff0fa3)

2. 생성 (프리셋)

![Image](https://github.com/user-attachments/assets/5cb55bd8-7a87-4ae2-ac42-7a76cd4acc73)

### GPIO 제어

![Image](https://github.com/user-attachments/assets/f315639a-8273-42a1-876d-5fecfd5a2f12)

## krz_gpio library
### INIT GpioController
```
GpioController gpioController = GpioController.getInstance(this);
gpioController.startObserver();
```
Gpio State Change Callback (Optional)
```
gpioController.setOnChangeGpioStateListener(new GpioController.OnChangeGpioStateListener() {
    @Override
    public void onGpioStateChanged(GpioState gpioState) {
        // TODO. something
    }
});
```

### ADD GPIO
```
Gpio gpio = gpioController.create(name, address);
gpioController.registerGpioObserver(gpio);
```

### READ GPIO
```
gpioController.getGpioState(gpioName);
```

### WRITE GPIO
```
Gpio gpio = gpioController.getGpio(name)
gpio.setDirection(Gpio.DIRECTION_OUT);
gpio.setActive(Gpio.ACTIVE_LOW);
gpioController.writeOnce(gpio);
```
### Preset

**GpioEnum.MS68** - M1010T, M1560T

**GpioEnum.S38** - RT4X, A1010T, A1560T, A2150T, A2400T, A3200T

**GpioEnum.S58** - RT5X, M2150T, M2400T, M3200T
