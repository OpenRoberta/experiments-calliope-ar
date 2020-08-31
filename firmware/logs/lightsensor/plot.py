import matplotlib.pyplot as plt
with open('03chaoticdata/01/01.txt', 'r') as f:
    lines = f.readlines()
    x = [float(line.split()[0]) for line in lines]
    #y = [float(line.split()[1]) for line in lines]
plt.ylabel('LightValue')
plt.xlabel('Measurement')
axes = plt.gca()
#axes.set_xlim([xmin,xmax])
axes.set_ylim([None,100])
plt.title('test with flashlight chaotic 01')
plt.plot(x, color='orange')
plt.show()
