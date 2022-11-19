#!/usr/bin/python

import matplotlib.pyplot as plt
import numpy as np

mydpi = 300
pltsize = (6, 2.0)


data = {
  'Chi-Squared'  : {
    'cingulata': 37.2,
    'e3': 221.78,
    't2': 44.3,
    'transpiler': 142.282,
    'romeo': 68.65,
  },
  'CRC-32'  : {
    'cingulata': 64.9,
    'e3': 680.2,
    't2': 66.6,
    'transpiler': 29.3272,
    'romeo': 156.205,
  },
}

cingulata = []
e3 = []
t2 = []
transpiler = []
romeo = []

x_axis_label = []
for k,val in data.items():
  x_axis_label.append(k)
  cingulata.append(val['cingulata'])
  e3.append(val['e3'])
  t2.append(val['t2'])
  transpiler.append(val['transpiler'])
  romeo.append(val['romeo'])

N = len(e3)
index = np.arange(N) # the x locations for the groups
width = 0.15 # the width of the bars

fig, ax = plt.subplots(figsize=pltsize)
ax.margins(0.02, 0.02)

rects1 = ax.bar(index - 3*width/2, t2, width,
                color='xkcd:light salmon', hatch='//', edgecolor='black', linewidth=1)
rects2 = ax.bar(index - width/2, cingulata, width,
                color='#ffddbf', hatch='xx', edgecolor='black', linewidth=1)
rects3 = ax.bar(index + width/2, e3, width,
                color='xkcd:ecru', hatch='xx', edgecolor='black', linewidth=1)
rects4 = ax.bar(index + 3*width/2, transpiler, width,
                color='xkcd:very light green', hatch='--', edgecolor='black', linewidth=1)
rects5 = ax.bar(index + 5*width/2, romeo, width,
                color='xkcd:very light blue', hatch='\\\\', edgecolor='black', linewidth=1)

ax.set_axisbelow(True)
ax.grid(True, axis='y', which="both", linewidth = "0.3", linestyle='--')
ax.set_yscale('log')
ax.set_ylim([1, 15000])
ax.set_yticks([1, 10, 100, 1000, 10000])
ax.set_ylabel("Time (sec.)")
ax.set_xlabel("Benchmark")
ax.set_xticks(index + width / 2)
ax.set_xticklabels(x_axis_label)
ax.legend((rects1[0], rects2[0], rects3[0], rects4[0], rects5[0]),
          ("T2", "Cingulata", "E3", "Transpiler", "Romeo"),
          fontsize=9, ncol=5, loc='upper center')

def autolabel_above(rects, lib_color):
  for rect in rects:
    height = rect.get_height()
    if height <= 0.014:
      ax.text(rect.get_x() + rect.get_width()/2., 2, 'N/S', color='black', bbox=dict(facecolor='none', color=lib_color, linewidth=5, boxstyle='square'), ha='center', va='bottom', fontsize=8, rotation=90)
      continue
    if height > 10:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.1f' % (height), ha='center', va='bottom', fontsize=7)
    else:
      ax.text(rect.get_x() + rect.get_width()/2., 1.1*height, '%2.2f' % (height), ha='center', va='bottom', fontsize=7)

autolabel_above(rects1, 'xkcd:light salmon')
autolabel_above(rects2, '#ffddbf')
autolabel_above(rects3, 'xkcd:ecru')
autolabel_above(rects4, 'xkcd:very light green')
autolabel_above(rects5, 'xkcd:very light blue')

# plt.show()

plt.tight_layout()
plt.savefig("./chi_crc_compilers.png", dpi=mydpi, bbox_inches="tight", pad_inches=0.03)
