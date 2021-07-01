raw  = pd.read_csv('src/input/ipums-cps-full.csv')
raw = raw[raw['YEAR'] >= 1977]
raw = raw.drop(columns = ['SEX', 'RACE', 'HISPAN', 'EDUC', 'INCTOT', 'INCWAGE', 'ASECWT', 'AGE']) 
raw.reset_index(inplace = True, drop = True)
raw.loc[:4000000].to_csv('ipums-cps-lite1.csv', index = False)
raw.loc[4000001:].to_csv('ipums-cps-lite2.csv', index = False)
