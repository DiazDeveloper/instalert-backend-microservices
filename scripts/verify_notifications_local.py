"""Real local delivery checks; JWTs remain in memory, evidence contains IDs only."""
import json,time,uuid,urllib.request,urllib.error
from pathlib import Path
from datetime import datetime,timezone
root=Path(__file__).resolve().parents[1];base='http://127.0.0.1:8080/api/v1';checks=[]
def call(method,path,body=None,token=None,headers=None,expected=200):
 h={'Content-Type':'application/json',**(headers or {})}
 if token:h['Authorization']='Bearer '+token
 req=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),headers=h,method=method)
 try:
  with urllib.request.urlopen(req,timeout=12) as r:code=r.status;data=r.read()
 except urllib.error.HTTPError as e:code=e.code;data=e.read()
 assert code==expected,(method,path,code,expected)
 return json.loads(data) if data else None

def check(name,ok=True):assert ok,name;checks.append(name);print('PASS:',name,flush=True)
def register(name):
 email='notices.'+name+'.'+uuid.uuid4().hex[:10]+'@example.com'
 return call('POST','/authentication/sign-up',{'name':'Avisos local '+name,'email':email,'password':'LocalNoticeTest2026!'},expected=201)
def inbox(user):return call('GET','/communities/notifications',token=user['token'])
def wait_notice(user,alert,event):
 for i in range(70):
  found=[x for x in inbox(user) if x['alert_id']==alert and x['event_type']==event]
  if found:return found
  time.sleep(1)
 raise AssertionError('Notice not received: '+event)
def create(user):
 payload={'type':'LOCAL_NOTIFICATION_TEST','latitude':-12.0464,'longitude':-77.0428,'accuracyM':15,'locationRecordedAt':datetime.now(timezone.utc).isoformat()};key=uuid.uuid4().hex
 return call('POST','/emergencies',payload,user['token'],{'Idempotency-Key':key},201),payload,key
if __name__=='__main__':
 owner,recipient,stranger=[register(x) for x in ('owner','recipient','stranger')];oid=owner['accountId'];rid=recipient['accountId']
 link=call('POST','/communities/contacts',{'accountId':rid},owner['token'],expected=201)['id']
 call('PUT','/communities/contacts/'+link+'/accept',{},owner['token'],expected=403);check('Requester cannot accept for recipient')
 early,_,_=create(owner);time.sleep(3);check('Pending invitation receives no location',not inbox(recipient))
 call('PUT','/communities/contacts/'+link+'/accept',{},recipient['token'],expected=204);check('Recipient explicitly accepts link')
 alert,payload,key=create(owner);aid=alert['id'];notices=wait_notice(recipient,aid,'EmergencyAlertCreated');check('Creation delivered to durable inbox',len(notices)==1)
 check('Original coordinates received',json.loads(notices[0]['payload'])['latitude']==-12.0464)
 check('Stranger receives no notice',not inbox(stranger));check('Owner does not impersonate recipient',not inbox(owner))
 call('POST','/emergencies',payload,owner['token'],{'Idempotency-Key':key});time.sleep(2);check('Replay creates no duplicate notice',len([n for n in inbox(recipient) if n['alert_id']==aid])==1)
 notice=notices[0]['id'];call('PUT','/communities/notifications/'+notice+'/read',{},stranger['token'],expected=404);check('Stranger cannot mark another inbox read')
 call('PUT','/communities/notifications/'+notice+'/read',{},recipient['token'],expected=204);check('Read state persisted',next(n for n in inbox(recipient) if n['id']==notice)['read_at'] is not None)
 call('PUT','/communities/notification-preferences',{'enabled':False},recipient['token'],expected=204)
 muted,_,_=create(owner);time.sleep(3);check('Opt-out blocks new alert',not [n for n in inbox(recipient) if n['alert_id']==muted['id']])
 call('PATCH','/emergencies/'+aid,{'status':'CANCELLED'},owner['token']);wait_notice(recipient,aid,'EmergencyAlertCancelled');check('Cancellation reaches original recipient after opt-out')
 call('PATCH','/emergencies/'+aid,{'status':'CANCELLED'},owner['token']);time.sleep(2);check('Repeated cancellation creates no duplicate',len([n for n in inbox(recipient) if n['alert_id']==aid])==2)
 call('POST','/communities/internal/emergency-events',{},owner['token'],expected=404);check('Gateway blocks internal event injection')
 call('GET','/communities/notifications',expected=401);check('Anonymous inbox denied')
 call('DELETE','/communities/contacts/'+link,token=recipient['token'],expected=204)
 revoked,_,_=create(owner);time.sleep(3);check('Revoked link receives no new alert',not [n for n in inbox(recipient) if n['alert_id']==revoked['id']])
 recovery=register('recovery');link2=call('POST','/communities/contacts',{'accountId':recovery['accountId']},owner['token'],expected=201)['id'];call('PUT','/communities/contacts/'+link2+'/accept',{},recovery['token'],expected=204)
 # Only non-sensitive IDs/emails saved for a later explicit sign-in. Never sessions.
 (root/'target/local-run/notification-test-accounts.json').write_text(json.dumps({'ownerEmail':owner['email'],'recipientEmail':recipient['email'],'recoveryEmail':recovery['email'],'alertId':aid}),encoding='utf-8')
 report={'target':'real local MySQL + Account + Gateway + Emergencies + Communities','checkedAt':datetime.now(timezone.utc).isoformat(),'passed':len(checks),'checks':checks,'alertId':aid,'ownerAccountId':oid,'recipientAccountId':rid,'channel':'durable in-app inbox'}
 (root/'target/local-run/notifications-verification.json').write_text(json.dumps(report,indent=2),encoding='utf-8');print('Passed',len(checks),'checks. Alert:',aid)
