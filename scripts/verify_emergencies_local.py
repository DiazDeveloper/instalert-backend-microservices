"""Real HTTP checks against local Gateway, Account and MySQL-backed Emergencies.
Creates two isolated sample accounts. Never logs credentials or tokens.
"""
import concurrent.futures
import json
import uuid
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError

BASE = 'http://127.0.0.1:8080/api/v1'
results = []

def call(method, path, body=None, token=None, key=None, base=BASE):
    headers = {'Content-Type': 'application/json'}
    if token: headers['Authorization'] = 'Bearer ' + token
    if key: headers['Idempotency-Key'] = key
    req = Request(base + path, data=json.dumps(body).encode() if body is not None else None, headers=headers, method=method)
    try:
        with urlopen(req, timeout=20) as response:
            data = response.read()
            return response.status, json.loads(data) if data else None
    except HTTPError as response:
        data = response.read()
        return response.code, json.loads(data) if data else None

def check(name, actual, expected):
    assert actual == expected, (name, actual, expected)
    results.append({'test': name, 'result': 'PASS'})

def run():
    accounts=[]
    for _ in range(2):
        password='LocalTest-' + uuid.uuid4().hex
        status, session=call('POST','/authentication/sign-up', {'name':'Emergency Local Test','email':'emergency-'+uuid.uuid4().hex+'@example.com','password':password})
        check('Register isolated sample account', status, 201)
        accounts.append((session['token'],password,session['accountId']))
    token,password,owner=accounts[0]
    other=accounts[1][0]
    payload={'type':'PANIC','latitude':-12.0464,'longitude':-77.0428,'accuracyM':15,'locationRecordedAt':datetime.now(timezone.utc).isoformat()}
    key=uuid.uuid4().hex
    status,alert=call('POST','/emergencies',payload,token,key)
    check('Create through Gateway',status,201)
    check('Owner derived from Account',alert['accountId'],owner)
    alert_id=alert['id']
    status,replay=call('POST','/emergencies',payload,token,key)
    check('Replay returns 200',status,200)
    check('Replay preserves UUID',replay['id'],alert_id)
    check('Key conflict',call('POST','/emergencies',{**payload,'type':'OTHER'},token,key)[0],409)
    check('Reject invalid coordinates',call('POST','/emergencies',{**payload,'latitude':100},token,uuid.uuid4().hex)[0],400)
    check('Reject forged owner',call('POST','/emergencies',{**payload,'userId':1},token,uuid.uuid4().hex)[0],400)
    check('No session',call('GET','/emergencies')[0],401)
    check('Invalid session',call('GET','/emergencies',token='invalid')[0],401)
    check('Other account cannot read',call('GET','/emergencies/'+alert_id,token=other)[0],403)
    check('Other account cannot cancel',call('PATCH','/emergencies/'+alert_id,{'status':'CANCELLED'},other)[0],403)
    check('Other history empty',call('GET','/emergencies',token=other)[1]['totalElements'],0)
    concurrent_key=uuid.uuid4().hex
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        responses=list(pool.map(lambda _:call('POST','/emergencies',payload,token,concurrent_key),range(4)))
    check('Concurrent calls accepted',all(s in (200,201) for s,_ in responses),True)
    check('Concurrent requests create one UUID',len({a['id'] for _,a in responses}),1)
    status,history=call('GET','/emergencies?status=ACTIVE&size=1&page=0',token=token)
    check('History pagination and status', (status,len(history['content']),history['totalElements']), (200,1,2))
    for current_id in {alert_id,*[a['id'] for _,a in responses]}:
        check('Cancel own alert',call('PATCH','/emergencies/'+current_id,{'status':'CANCELLED'},token)[0],200)
    check('Repeat cancel safely',call('PATCH','/emergencies/'+alert_id,{'status':'CANCELLED'},token)[0],200)
    check('Cancelled history',call('GET','/emergencies?status=CANCELLED',token=token)[1]['totalElements'],2)
    check('Monolith no longer exposes emergencies',call('GET','/emergencies',token=token,base='http://127.0.0.1:8082/api/v1')[0],404)
    check('Incidents still available',call('GET','/incidents',token=token)[0],200)
    check('Communities still available',call('GET','/communities',token=token)[0],200)
    check('Password rotation',call('POST',f'/authentication/{owner}/change-password',{'currentPassword':password,'newPassword':password+'-rotated'},token)[0],204)
    check('Revoked session rejected by emergencies',call('GET','/emergencies',token=token)[0],401)
    report={'executedAt':datetime.now(timezone.utc).isoformat(),'environment':'Local Gateway + real Account + MySQL','checks':results,'notificationDelivery':'Not implemented; outbox events remain PENDING'}
    output=Path(__file__).resolve().parents[1]/'target/local-run/emergencies-verification.json'
    output.parent.mkdir(parents=True,exist_ok=True);output.write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(f'{len(results)} real HTTP checks passed. Report: {output}')

if __name__=='__main__': run()
