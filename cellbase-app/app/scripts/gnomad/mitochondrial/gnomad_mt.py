import sys
import gzip


POPULATIONS = ['afr', 'ami', 'amr', 'asj', 'eas', 'fin', 'nfe', 'oth', 'sas', 'mid']
HEADER_COMMON = [
    '##INFO=<ID=AC,Number=1,Type=Integer,Description="Calculated allele count">',
    '##INFO=<ID=AF,Number=1,Type=Float,Description="Calculated allele frequency">',
    '##INFO=<ID=GTC,Number=1,Type=String,Description="Calculated list of genotype counts (0/0,0/1,1/1)">'
]
HEADER_POP = [
    '##INFO=<ID=AF_{pop},Number=1,Type=Float,Description="Calculated allele frequency for {pop} population">',
    '##INFO=<ID=AC_{pop},Number=1,Type=Integer,Description="Calculated allele count for {pop} population">',
    '##INFO=<ID=AN_{pop},Number=1,Type=Integer,Description="Calculated allele number for {pop} population">',
    '##INFO=<ID=GTC_{pop},Number=1,Type=String,Description="Calculated list of genotype counts for {pop} population (0/0,0/1,1/1)">'
]


def main():

    # Creating custom header
    custom_header = []
    custom_header += HEADER_COMMON
    for pop in POPULATIONS:
        custom_header += ['\n'.join(HEADER_POP).format(pop=pop)]
    custom_header = '\n'.join(custom_header) + '\n'

    # Opening input/output files
    vcf_input_fpath = sys.argv[1]
    vcf_output_fpath = sys.argv[2]
    vcf_input_fhand = gzip.open(vcf_input_fpath, 'r')
    vcf_output_fhand = gzip.open(vcf_output_fpath, 'wt')

    # Calculating new INFO fields for each variant
    for line in vcf_input_fhand:
        line = line.decode()

        # Writing header to output
        if line.startswith('##VEP'):  # adding custom header before "##VEP" line
            vcf_output_fhand.write(custom_header)
            vcf_output_fhand.write(line)
            continue
        if line.startswith('#'):
            vcf_output_fhand.write(line)
            continue

        # Dict to store the new calculated data
        new_info = {}

        # Getting variant and INFO data
        variant_items = line.strip().split()
        info_items = variant_items[7].split(';')

        for info_item in info_items:

            # Getting key/value for each INFO item
            if len(info_item.split('=', maxsplit=1)) < 2:  # skipping flags
                continue
            info_key, info_value = info_item.split('=', maxsplit=1)

            # Getting INFO data for calculations
            if info_key == 'pop_AF_hom':
                pop_AF_hom = list(map(float, info_value.split('|')))
            if info_key == 'pop_AF_het':
                pop_AF_het = list(map(float, info_value.split('|')))
            if info_key == 'AF_hom':
                AF_hom = float(info_value)
            if info_key == 'AF_het':
                AF_het = float(info_value)
            if info_key == 'pop_AC_hom':
                pop_AC_hom = list(map(int, info_value.split('|')))
            if info_key == 'pop_AC_het':
                pop_AC_het = list(map(int, info_value.split('|')))
            if info_key == 'AC_hom':
                AC_hom = int(info_value)
            if info_key == 'AC_het':
                AC_het = int(info_value)
            if info_key == 'pop_AN':
                pop_AN = list(map(int, info_value.split('|')))
            if info_key == 'AN':
                AN = int(info_value)

        # Calculating AF_{pop} and AF
        # e.g. AF_sas = pop_AF_hom[i] + pop_AF_het[i] (i = index of sas population)
        pop_AF = [x + y for x, y in zip(pop_AF_hom, pop_AF_het)]
        for i, pop in enumerate(POPULATIONS):
            new_info['AF_' + pop] = pop_AF[i]
        new_info['AF'] = AF_hom + AF_het

        # Calculating AC_{pop} and AC
        # e.g. AC_sas = pop_AC_hom[i] + pop_AC_het[i] (i = index of sas population)
        pop_AC = [x + y for x, y in zip(pop_AC_hom, pop_AC_het)]
        for i, pop in enumerate(POPULATIONS):
            new_info['AC_' + pop] = pop_AC[i]
        new_info['AC'] = AC_hom + AC_het

        # Calculating AN_{pop}
        # e.g. AN_sas = pop_AN[i] (i = index of sas population)
        for i, pop in enumerate(POPULATIONS):
            new_info['AN_' + pop] = pop_AN[i]

        # Calculating GTC_{pop}
        # e.g. GTC_sas = (pop_AN[i] - (pop_AC_het[i] + pop_AC_hom[i])) + "," + pop_AC_het[i] + "," + pop_AC_hom[i]
        pop_AC = [x + y for x, y in zip(pop_AC_hom, pop_AC_het)]
        hom_ref = [x - y for x, y in zip(pop_AN, pop_AC)]
        for i, pop in enumerate(POPULATIONS):
            new_info['GTC_' + pop] = ','.join(map(str, [hom_ref[i], pop_AC_het[i], pop_AC_hom[i]]))
        new_info['GTC'] = ','.join(map(str, [AN - AC_hom + AC_het, AC_het, AC_hom]))

        # Joining existing INFO field and new custom INFO data
        custom_info_data = ';'.join(['='.join([k, str(new_info[k])]) for k in new_info])
        new_info_field = ';'.join(info_items + [custom_info_data])

        # Replacing original INFO field
        variant_items[7] = new_info_field
        vcf_output_fhand.write('\t'.join(variant_items) + '\n')


if __name__ == '__main__':
    sys.exit(main())
