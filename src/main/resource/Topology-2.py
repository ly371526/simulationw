from mininet.topo import Topo

class MyTopo(Topo):

    def __init__(self):

        Topo.__init__(self)

        LEOOrbitTotal = 66
        LEONumEachOrbit = 24
        
        LEOs = []
        for L in range(1, LEOOrbitTotal * LEONumEachOrbit + 1):
            LEO = self.addSwitch('S-%s' %L)
            LEOs.append(LEO)

        LEOLists = []
        for i in range(1, LEOOrbitTotal + 1):
            LEOList = []
            LEOLists.append(LEOList)

        
        for LEO in LEOs:
            n = LEOs.index(LEO)
            l = len(LEOs)
            
            for LEOList in LEOLists:
                if n < LEOLists.index(LEOList) * LEONumEachOrbit:
                    LEOList.append(LEOs[n])

            for z in range(n + 1, l):
                x = z - n
                if x == LEONumEachOrbit:
                    self.addLink(LEO, LEOs[z])
                elif x == LEONumEachOrbit * (LEOOrbitTotal - 1):
                    self.addLink(LEO, LEOs[z])
                elif x == 1 and (n + 1) % LEONumEachOrbit != 0:
                    self.addLink(LEO, LEOs[z])
                elif x == LEONumEachOrbit - 1 and (z + 1) % LEONumEachOrbit == 0:
                    self.addLink(LEO, LEOs[z])



        # for LEOList in LEOLists:
        #     for LEO in LEOList:
        #         n = LEOList.index(LEO)
        #         l = len(LEOList)
        #         for z in range(n + 1, l):
        #             x = z - n
        #             if x == 1:
        #                 self.addLink(LEO, LEOList[z])
        #             elif x == LEONumEachOrbit - 1:
        #                 self.addLink(LEO, LEOList[z])



topos = {'mytopo': (lambda: MyTopo())}