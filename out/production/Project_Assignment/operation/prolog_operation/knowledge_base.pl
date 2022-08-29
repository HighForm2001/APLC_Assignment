%country() = countries in the list
country(afghanistan).
country(albania).
country(algeria).
country(andorra).
country(angola).
country(antarctica).
country(antigua_and_barbuda).
country(argentina).
country(armenia).
country(australia).
country(austria).
country(azerbaijan).
country(bahamas).
country(bahrain).
country(bangladesh).
country(barbados).
country(belarus).
country(belgium).
country(belize).
country(benin).
country(bhutan).
country(bolivia).
country(bosnia_and_herzegovina).
country(botswana).
country(brazil).
country(brunei).
country(bulgaria).
country(burkina_faso).
country(burma).
country(burundi).
country(cabo_verde).
country(cambodia).
country(cameroon).
country(canada).
country(central_african_republic).
country(chad).
country(chile).
country(china).
country(colombia).
country(comoros).
country(congo_brazzaville).
country(congo_kinshasa).
country(costa_rica).
country(cote_d_ivoire).
country(croatia).
country(cuba).
country(cyprus).
country(czechia).
country(denmark).
country(diamond_princess).
country(djibouti).
country(dominica).
country(dominican_republic).
country(ecuador).
country(egypt).
country(el_salvador).
country(equatorial_guinea).
country(eritrea).
country(estonia).
country(eswatini).
country(ethiopia).
country(fiji).
country(finland).
country(france).
country(gabon).
country(gambia).
country(georgia).
country(germany).
country(ghana).
country(greece).
country(grenada).
country(guatemala).
country(guinea).
country(guinea_bissau).
country(guyana).
country(haiti).
country(holy_see).
country(honduras).
country(hungary).
country(iceland).
country(india).
country(indonesia).
country(iran).
country(iraq).
country(ireland).
country(israel).
country(italy).
country(jamaica).
country(japan).
country(jordan).
country(kazakhstan).
country(kenya).
country(kiribati).
country(korea_north).
country(korea_south).
country(kosovo).
country(kuwait).
country(kyrgyzstan).
country(laos).
country(latvia).
country(lebanon).
country(lesotho).
country(liberia).
country(libya).
country(liechtenstein).
country(lithuania).
country(luxembourg).
country(ms_zaandam).
country(madagascar).
country(malawi).
country(malaysia).
country(maldives).
country(mali).
country(malta).
country(marshall_islands).
country(mauritania).
country(mauritius).
country(mexico).
country(micronesia).
country(moldova).
country(monaco).
country(mongolia).
country(montenegro).
country(morocco).
country(mozambique).
country(namibia).
country(nepal).
country(netherlands).
country(new_zealand).
country(nicaragua).
country(niger).
country(nigeria).
country(north_macedonia).
country(norway).
country(oman).
country(pakistan).
country(palau).
country(panama).
country(papua_new_guinea).
country(paraguay).
country(peru).
country(philippines).
country(poland).
country(portugal).
country(qatar).
country(romania).
country(russia).
country(rwanda).
country(saint_kitts_and_nevis).
country(saint_lucia).
country(saint_vincent_and_the_grenadines).
country(samoa).
country(san_marino).
country(sao_tome_and_principe).
country(saudi_arabia).
country(senegal).
country(serbia).
country(seychelles).
country(sierra_leone).
country(singapore).
country(slovakia).
country(slovenia).
country(solomon_islands).
country(somalia).
country(south_africa).
country(south_sudan).
country(spain).
country(sri_lanka).
country(sudan).
country(summer_olympics_2020).
country(suriname).
country(sweden).
country(switzerland).
country(syria).
country(taiwan).
country(tajikistan).
country(tanzania).
country(thailand).
country(timor_leste).
country(togo).
country(tonga).
country(trinidad_and_tobago).
country(tunisia).
country(turkey).
country(us).
country(uganda).
country(ukraine).
country(united_arab_emirates).
country(united_kingdom).
country(uruguay).
country(uzbekistan).
country(vanuatu).
country(venezuela).
country(vietnam).
country(west_bank_and_gaza).
country(winter_olympics_2022).
country(yemen).
country(zambia).
country(zimbabwe).

%make this dynamic so that it can record the latest information based on the csv
:- dynamic(cases_country/2).

create_list([],List,List).
create_list([Head|Tail],List,[Head|Rest]) :- create_list(Tail,List,Rest).

merge_sort([], []).            
merge_sort([X], [X]).          
merge_sort(Unsorted, Sorted) :- 
    partition( Unsorted, L, R) , 
    sort(L,L1),                   
    sort(R,R1),               
    merge(L1,R1,Sorted).         
    

partition([], [], []).  
partition([X], [X], []).  
partition([X,Y|L], [X|Xs], [Y|Ys]) :- 
    partition(L,Xs,Ys).                 

merge([], [], []).       
merge([], [Y|Ys] , [Y|Ys]).        
merge([X|Xs] , [], [X|Xs]).       
merge([X|Xs] , [Y|Ys] , [Lo,Hi|Zs]) :-  
    compare(X,Y,Lo,Hi),                  
    merge(Xs,Ys,Zs).                      

compare(X, Y, X, Y) :- X @=< Y.
compare(X, Y, Y, X) :- X @>  Y. 

reverseList([H|T], ReversedList):-
reverseListHelper(T,[H], ReversedList).
reverseListHelper([], Acc, Acc).
reverseListHelper([H|T], Acc, ReversedList):-
reverseListHelper(T, [H|Acc], ReversedList).


